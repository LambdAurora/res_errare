package dev.lambdaurora.res_errare;

import dev.lambdaurora.res_errare.render.shader.Shader;
import dev.lambdaurora.res_errare.render.shader.ShaderProgram;
import dev.lambdaurora.res_errare.render.shader.ShaderType;
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

		var fragmentSrc = """
				#version 330 core
				    
				in vec3 normal;
				in vec2 texture_coords;
				    
				out vec4 FragColor;
				    
				uniform sampler2D texture_diffuse1;
				uniform sampler2D texture_normal1;
				uniform sampler2D texture_specular1;
				    
				void main() {
				    vec4 texture_color = texture(texture_diffuse1, texture_coords);
				    if (texture_color.a < 0.1)
				        discard;
				    FragColor = texture_color;
				}
				""";

		var result = new ShaderProgram.Builder()
				.shader(Shader.compile(ShaderType.FRAGMENT, fragmentSrc))
				.shader(Shader.compile(ShaderType.VERTEX, shaderSource))
				.withCleanup()
				.build();
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

			shader.use();

			GLFW.pollEvents();
			window.swapBuffers();
		}

		shader.delete();
		window.destroy();
		GLFW.terminate();
	}

	public static int getRainbowRGB(double x, double y) {
		float speed = 8600.0f;
		return Color.HSBtoRGB((float) ((System.currentTimeMillis() - x * 10.0D - y * 10.0D) % speed) / speed,
				.75f, .9f);
	}
}
