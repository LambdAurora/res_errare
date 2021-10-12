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
	private Camera camera = new Camera();
	private GameRenderer renderer;
	private Skybox skybox;

	public ResErrare(String title) throws IOException {
		this.window = Window.create(640, 480, title).orElseThrow();

		this.window.makeContextCurrent();
		GLFW.swapInterval(1);

		this.renderer = new GameRenderer();

		var skybox = Skybox.of(CubeMapTexture.builder()
				.face(CubeMapTexture.CubeMapTextureTarget.POSITIVE_X, new Identifier(Constants.NAMESPACE, "textures/skybox/right.jpg"))
				.face(CubeMapTexture.CubeMapTextureTarget.NEGATIVE_X, new Identifier(Constants.NAMESPACE, "textures/skybox/left.jpg"))
				.face(CubeMapTexture.CubeMapTextureTarget.POSITIVE_Y, new Identifier(Constants.NAMESPACE, "textures/skybox/top.jpg"))
				.face(CubeMapTexture.CubeMapTextureTarget.NEGATIVE_Y, new Identifier(Constants.NAMESPACE, "textures/skybox/bottom.jpg"))
				.face(CubeMapTexture.CubeMapTextureTarget.POSITIVE_Z, new Identifier(Constants.NAMESPACE, "textures/skybox/back.jpg"))
				.face(CubeMapTexture.CubeMapTextureTarget.NEGATIVE_Z, new Identifier(Constants.NAMESPACE, "textures/skybox/front.jpg"))
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
			if (action == 1 && key == 256)
				this.running = false;
		});

		this.camera.setPosition(0, 0, 3);
		this.camera.setYaw(-90.f);
	}

	public void run() {
		GL.get().enable(GL.GL11.CULL_FACE);
		GL.get().enable(GL.GL11.DEPTH_TEST);

		while (this.running) {
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
		int color = getRainbowRGB(0, 0);
		GL.get().clear(GL.GL11.COLOR_BUFFER_BIT | GL.GL11.DEPTH_BUFFER_BIT);
		GL.get().clearColor(((color >> 16) & 255) / 255.f,
				((color >> 8) & 255) / 255.f,
				(color & 255) / 255.f,
				1.f);

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
