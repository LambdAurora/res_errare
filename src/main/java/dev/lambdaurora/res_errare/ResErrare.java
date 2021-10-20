/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.lambdaurora.res_errare;

import dev.lambdaurora.res_errare.input.ButtonAction;
import dev.lambdaurora.res_errare.render.Camera;
import dev.lambdaurora.res_errare.render.GameRenderer;
import dev.lambdaurora.res_errare.render.Skybox;
import dev.lambdaurora.res_errare.render.graphics.Graphics2D;
import dev.lambdaurora.res_errare.render.shader.Shader;
import dev.lambdaurora.res_errare.render.shader.ShaderProgram;
import dev.lambdaurora.res_errare.render.shader.ShaderType;
import dev.lambdaurora.res_errare.render.texture.*;
import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.system.GLFW;
import dev.lambdaurora.res_errare.util.Identifier;
import dev.lambdaurora.res_errare.window.Window;

import java.awt.*;
import java.io.IOException;

public final class ResErrare {
	private Window window;
	private boolean running = true;
	private boolean wireframe = false;
	private Camera camera = new Camera();
	private GameRenderer renderer;
	private Skybox skybox;
	private ShaderProgram voxelSpaceShader;
	private Texture2D outputTexture;
	private float yaw = 0.f;
	private float horizon = 15.f;

	public ResErrare(String title) throws IOException {
		this.window = Window.create(640, 480, title).orElseThrow();

		this.window.makeContextCurrent();
		GLFW.swapInterval(1);

		System.out.println("Using: " + GL.get().getString(GL.GL11.RENDERER));

		this.renderer = new GameRenderer();

		this.skybox = Skybox.of(CubeMapTexture.builder()
						.facesFromDirectory(new Identifier(Constants.NAMESPACE, "textures/skybox"), "jpg")
						.withCleanup()
						.build())
				.getOrThrow();
		this.skybox.scale(50.f);

		this.voxelSpaceShader = ShaderProgram.builder()
				.shader(Shader.compile(ShaderType.COMPUTE, new Identifier(Constants.NAMESPACE, "voxelspace/shader")))
				.build().getOrThrow();

		this.init();
	}

	private void init() {
		this.window.setFramebufferSizeCallback((width, height) -> {
			this.outputTexture.initEmpty(Texture2D.Target.TEXTURE_2D, 0, Texture.InternalFormat.RGBA32F, width, height);
			this.renderer.setupProjection(width, height);
		});
		this.window.setKeyCallback((key, scancode, action, mods) -> {
			if (action == ButtonAction.RELEASE) {
				switch (key) {
					case 256 -> this.running = false;
					case 257 -> {
						this.wireframe = !this.wireframe;
						GL.get().polygonMode(GL.GL11.FRONT_AND_BACK, this.wireframe ? GL.GL11.LINE : GL.GL11.FILL);
					}
				}
			}
		});

		this.camera.setPosition(0, 148, 3);

		this.voxelSpaceShader.use();
		this.voxelSpaceShader.setInt("heightmap", 1);
		this.voxelSpaceShader.setInt("colormap", 2);
		this.voxelSpaceShader.setInt("height", 50);
		this.voxelSpaceShader.setInt("size", 1024);
	}

	public void run() {
		GL.get().enable(GL.GL11.CULL_FACE);
		GL.get().enable(GL.GL11.DEPTH_TEST);

		Texture2D heightmapTexture, colormapTexture;
		try {
			heightmapTexture = Texture2D.builder(new Identifier(Constants.NAMESPACE, "textures/heightmap.png"))
					.parameter(TextureParameters.MIN_FILTER, TextureParameters.FilterValue.NEAREST)
					.parameter(TextureParameters.MAG_FILTER, TextureParameters.FilterValue.NEAREST)
					.build();
			colormapTexture = Texture2D.builder(new Identifier(Constants.NAMESPACE, "textures/colormap.png"))
					.parameter(TextureParameters.MIN_FILTER, TextureParameters.FilterValue.NEAREST)
					.parameter(TextureParameters.MAG_FILTER, TextureParameters.FilterValue.NEAREST)
					.build();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		this.outputTexture = Texture2D.of(800, 600, Texture.InternalFormat.RGBA32F);

		var graphics = Graphics2D.get();

		float deltaTime; // Time between current frame and last frame.
		float lastFrame = 0.f;
		boolean direction = true;
		while (this.running) {
			float currentFrame = GLFW.getTime();
			deltaTime = currentFrame - lastFrame;
			lastFrame = currentFrame;

			float newYaw = this.camera.getYaw() + .5f;
			float newPitch = this.camera.getPitch() + (direction ? 1 : -1) * .25f;
			if (newPitch > 89) direction = false;
			else if (newPitch < -89) direction = true;
			//this.camera.setAngles(newYaw, newPitch);

			this.renderer.updateView(this.camera.getViewMatrix());

			GL.get().clear(GL.GL11.COLOR_BUFFER_BIT | GL.GL11.DEPTH_BUFFER_BIT);
			GL.get().clearColor(0.f, 0.f, 0.f, 1.f);

			this.voxelSpaceShader.use();
			this.voxelSpaceShader.setVec3f("pos", this.camera.getPosition());
			this.voxelSpaceShader.setFloat("yaw", (float) Math.toRadians(yaw));
			this.voxelSpaceShader.setFloat("pitch", horizon);
			outputTexture.bind();
			try (var ignored = outputTexture.bindImageTexture(0, 0, GL.Access.WRITE_ONLY, Texture.InternalFormat.RGBA32F)) {
				heightmapTexture.bind();
				var boundHeightmap = heightmapTexture.bindImageTexture(1, 0, GL.Access.READ_ONLY, Texture.InternalFormat.RGBA32F);
				colormapTexture.bind();
				var boundColormap = colormapTexture.bindImageTexture(2, 0, GL.Access.READ_ONLY, Texture.InternalFormat.RGBA32F);
				var dimensions = this.window.getFramebufferSize();
				this.voxelSpaceShader.dispatchCompute(dimensions.width(), 1, 1);
				GL.get().memoryBarrier(GL.GL42.SHADER_IMAGE_ACCESS_BARRIER_BIT);
				boundHeightmap.close();
				boundColormap.close();
			}
			Texture.unbind(TextureType.TEXTURE_2D);
			ShaderProgram.useNone();

			graphics.drawScreen(outputTexture);

			//this.render();

			GLFW.pollEvents();
			this.window.swapBuffers();
			this.processInput(deltaTime);
			this.running &= !this.window.shouldClose();
		}

		this.voxelSpaceShader.close();
	}

	public static void main(String[] args) throws IOException {
		GLFW.init();
		GLFW.windowHint(GLFW.CONTEXT_VERSION_MAJOR, 4);
		GLFW.windowHint(GLFW.CONTEXT_VERSION_MINOR, 3);
		GLFW.windowHint(GLFW.OPENGL_PROFILE, GLFW.OPENGL_CORE_PROFILE);

		var game = new ResErrare("res_errare Test");

		game.run();

		terminate();
	}

	public void render() {
		this.skybox.draw();
	}

	public void processInput(float deltaTime) {
		if (this.window.getKey(' ').isPressing()) {
			this.camera.getPosition().y++;
		} else if (this.window.getKey(340).isPressing()) {
			this.camera.getPosition().y--;
		}

		if (this.window.getKey('W').isPressing())
			this.processMovement(1, deltaTime);
		if (this.window.getKey('S').isPressing())
			this.processMovement(2, deltaTime);
		if (this.window.getKey('A').isPressing())
			this.processMovement(3, deltaTime);
		if (this.window.getKey('D').isPressing())
			this.processMovement(4, deltaTime);

		if (this.window.getKey(262).isPressing()) {
			this.yaw--;
			this.camera.setYaw(-this.yaw);
		} else if (this.window.getKey(263).isPressing()) {
			this.yaw++;
			this.camera.setYaw(-this.yaw);
		}
		if (this.window.getKey(264).isPressing())
			this.horizon -= 5f;
		else if (this.window.getKey(265).isPressing())
			this.horizon += 5f;
	}

	private void processMovement(int direction, float deltaTime) {
		float velocity = /*2.5f **/ 1;
		var front = camera.frontVector();
		var right = camera.rightVector();
		switch (direction) {
			case 1 -> camera.getPosition().add(front.x * velocity, front.y * velocity, front.z * velocity);
			case 2 -> camera.getPosition().sub(front.x * velocity, front.y * velocity, front.z * velocity);
			case 3 -> camera.getPosition().sub(right.x * velocity, right.y * velocity, right.z * velocity);
			case 4 -> camera.getPosition().add(right.x * velocity, right.y * velocity, right.z * velocity);
		}
	}

	public static void terminate() {
		Skybox.terminate();
		GLFW.terminate();
	}

	public static int getRainbowRGB(double x, double y) {
		float speed = 8600.0f;
		return Color.HSBtoRGB((float) ((System.currentTimeMillis() - x * 10.0D - y * 10.0D) % speed) / speed,
				.75f, .9f);
	}
}
