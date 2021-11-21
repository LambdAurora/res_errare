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

package dev.lambdaurora.res_errare.render.graphics;

import dev.lambdaurora.res_errare.Constants;
import dev.lambdaurora.res_errare.render.GeometricPrimitive;
import dev.lambdaurora.res_errare.render.array.VertexArray;
import dev.lambdaurora.res_errare.render.array.VertexLayout;
import dev.lambdaurora.res_errare.render.buffer.BufferTarget;
import dev.lambdaurora.res_errare.render.buffer.GraphicsBuffer;
import dev.lambdaurora.res_errare.render.shader.Shader;
import dev.lambdaurora.res_errare.render.shader.ShaderProgram;
import dev.lambdaurora.res_errare.render.shader.ShaderType;
import dev.lambdaurora.res_errare.render.texture.Texture;
import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.util.Identifier;

import java.util.List;

public class Graphics2D {
	private static Graphics2D self = null;
	private final ShaderProgram framebufferShader;
	private final VertexArray screenQuadVao;

	public Graphics2D(ShaderProgram framebufferShader, VertexArray screenQuadVao) {
		this.framebufferShader = framebufferShader;
		this.screenQuadVao = screenQuadVao;
	}

	public static Graphics2D get() {
		if (self == null) {
			var screenQuadVao = new VertexArray();
			screenQuadVao.bind();
			var screenQuadVbo = GraphicsBuffer.ofStatic(BufferTarget.ARRAY, SCREEN_QUAD_VERTICES);
			screenQuadVao.useLayout(new VertexLayout(List.of(VertexLayout.VEC2F_ELEMENT, VertexLayout.VEC2F_ELEMENT)));
			screenQuadVbo.unbind();

			self = new Graphics2D(ShaderProgram.builder()
					.shader(Shader.compile(ShaderType.FRAGMENT, FRAMEBUFFER_SHADER_ID))
					.shader(Shader.compile(ShaderType.VERTEX, FRAMEBUFFER_SHADER_ID))
					.build().getOrThrow(), screenQuadVao);
		}

		return self;
	}

	public void drawScreenWith(Texture<?> texture, ShaderProgram program) {
		program.use();
		GL.get().activeTexture(GL.GL13.TEXTURE0);
		texture.bind();
		this.screenQuadVao.draw(GeometricPrimitive.TRIANGLES, 0, 6);
	}

	public void drawScreen(Texture<?> texture) {
		this.drawScreenWith(texture, this.framebufferShader);
	}

	public static final Identifier FRAMEBUFFER_SHADER_ID = new Identifier(Constants.NAMESPACE, "framebuffer");

	private static final float[] TEXTURED_QUAD_VERTICES = {
			// positions (2) ; texture coords (2)
			0.f, 1.f, 0.f, 1.f,
			0.f, 0.f, 0.f, 0.f,
			1.f, 0.f, 1.f, 0.f,

			0.f, 1.f, 0.f, 1.f,
			1.f, 0.f, 1.f, 0.f,
			1.f, 1.f, 1.f, 1.f
	};
	private static final float[] SCREEN_QUAD_VERTICES = {
			// positions (2) ; texture coords (2)
			-1.f, 1.f, 0.f, 1.f,
			-1.f, -1.f, 0.f, 0.f,
			1.f, -1.f, 1.f, 0.f,

			-1.f, 1.f, 0.f, 1.f,
			1.f, -1.f, 1.f, 0.f,
			1.f, 1.f, 1.f, 1.f
	};
}
