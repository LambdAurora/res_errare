package dev.lambdaurora.res_errare;

import dev.lambdaurora.res_errare.render.Shader;
import dev.lambdaurora.res_errare.render.ShaderType;
import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.system.GLFW;
import dev.lambdaurora.res_errare.window.Window;

import java.awt.*;

public final class ResErrare {
	public static void main(String[] args) {
		GLFW.init();

		var window = Window.create(640, 480, "res_errare Test").orElseThrow();

		window.makeContextCurrent();

		var shaderSource = """
				#version 330 core
				    
				layout (location = 0) in vec3 aPos;
				layout (location = 1) in vec3 aNormal;
				layout (location = 2) in vec2 aTexCoords;
				    
				layout (std140) uniform matrices {
				    mat4 projection;
				    mat4 view;
				};
				uniform mat4 model;
				    
				out vec3 normal;
				out vec2 texture_coords;
				    
				void main() {
				    gl_Position = projection * view * model * vec4(aPos, 1.0);
				    normal = aNormal;
				    texture_coords = aTexCoords;
				}
				""";

		var result = Shader.compile(ShaderType.VERTEX, shaderSource);
		if (result.hasError())
			throw result.getError();

		var shader = result.get();

		while (!window.shouldClose()) {
			int color = getRainbowRGB(0, 0);
			GL.get().clear(GL.GL11.GL_COLOR_BUFFER_BIT | GL.GL11.GL_DEPTH_BUFFER_BIT);
			GL.get().clearColor(((color >> 16) & 255) / 255.f,
					((color >> 8) & 255) / 255.f,
					(color & 255) / 255.f,
					1.f);

			GLFW.pollEvents();
			window.swapBuffers();
		}

		window.destroy();
		GLFW.terminate();
	}

	public static int getRainbowRGB(double x, double y) {
		float speed = 3600.0f;
		return Color.HSBtoRGB((float) ((System.currentTimeMillis() - x * 10.0D - y * 10.0D) % speed) / speed,
				.75f, .9f);
	}
}
