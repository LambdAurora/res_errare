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

import dev.lambdaurora.res_errare.render.Camera;
import dev.lambdaurora.res_errare.render.GameRenderer;
import dev.lambdaurora.res_errare.render.Skybox;
import dev.lambdaurora.res_errare.render.shader.Shader;
import dev.lambdaurora.res_errare.render.shader.ShaderProgram;
import dev.lambdaurora.res_errare.render.shader.ShaderType;
import dev.lambdaurora.res_errare.render.texture.CubeMapTexture;
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

	public ResErrare(String title) throws IOException {
		this.window = Window.create(640, 480, title).orElseThrow();

		this.window.makeContextCurrent();
		GLFW.swapInterval(1);

		this.renderer = new GameRenderer();

		var skybox = Skybox.of(CubeMapTexture.builder()
				.facesFromDirectory(new Identifier(Constants.NAMESPACE, "textures/skybox"), "jpg")
				.build());
		if (skybox.hasError())
			throw skybox.getError();
		this.skybox = skybox.get();
		this.skybox.scale(50.f);

		this.init();
	}

	private void init() {
		this.window.setFramebufferSizeCallback(this.renderer::setupProjection);
		this.window.setKeyCallback((key, scancode, action, mods) -> {
			if (action == 0) {
				switch (key) {
					case 256 -> this.running = false;
					case 257 -> {
						this.wireframe = !this.wireframe;
						GL.get().polygonMode(GL.GL11.FRONT_AND_BACK, this.wireframe ? GL.GL11.LINE : GL.GL11.FILL);
					}
				}
			}
		});

		this.camera.setPosition(0, 0, 3);
		this.camera.setYaw(-90.f);
	}

	public void run() {
		GL.get().enable(GL.GL11.CULL_FACE);
		GL.get().enable(GL.GL11.DEPTH_TEST);

		while (this.running) {
			float newYaw = this.camera.getYaw() + 1f;
			this.camera.setYaw(newYaw);

			this.renderer.updateView(this.camera.getViewMatrix());

			this.render();

			GLFW.pollEvents();
			this.window.swapBuffers();
			this.running &= !this.window.shouldClose();
		}
	}

	public static void main(String[] args) throws IOException {
		GLFW.init();
		GLFW.windowHint(GLFW.CONTEXT_VERSION_MAJOR, 3);
		GLFW.windowHint(GLFW.CONTEXT_VERSION_MINOR, 3);
		GLFW.windowHint(GLFW.OPENGL_PROFILE, GLFW.OPENGL_CORE_PROFILE);

		var game = new ResErrare("res_errare Test");

		var result = new ShaderProgram.Builder()
				.shader(Shader.compile(ShaderType.FRAGMENT, new Identifier(Constants.NAMESPACE, "shader")))
				.shader(Shader.compile(ShaderType.VERTEX, new Identifier(Constants.NAMESPACE, "shader")))
				.withCleanup()
				.build();
		if (result.hasError())
			throw result.getError();

		var shader = result.get();

		game.run();

		shader.close();
		terminate();
	}

	public void render() {
		GL.get().clear(GL.GL11.COLOR_BUFFER_BIT | GL.GL11.DEPTH_BUFFER_BIT);
		GL.get().clearColor(0.f, 0.f, 0.f, 1.f);

		this.skybox.draw();
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
