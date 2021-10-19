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

package dev.lambdaurora.res_errare.render;

import dev.lambdaurora.res_errare.Constants;
import dev.lambdaurora.res_errare.render.array.VertexArray;
import dev.lambdaurora.res_errare.render.array.VertexLayout;
import dev.lambdaurora.res_errare.render.buffer.BufferTarget;
import dev.lambdaurora.res_errare.render.buffer.GraphicsBuffer;
import dev.lambdaurora.res_errare.render.shader.Shader;
import dev.lambdaurora.res_errare.render.shader.ShaderProgram;
import dev.lambdaurora.res_errare.render.shader.ShaderType;
import dev.lambdaurora.res_errare.render.texture.CubeMapTexture;
import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.util.Identifier;
import dev.lambdaurora.res_errare.util.Result;

import java.util.List;

/**
 * Represents a skybox.
 */
public class Skybox implements AutoCloseable {
	public static final Identifier SHADER_ID = new Identifier(Constants.NAMESPACE, "skybox");
	private static final VertexLayout VAO_LAYOUT = new VertexLayout(List.of(VertexLayout.VEC3F_ELEMENT));
	private static final VertexArray SKYBOX_VAO = new VertexArray();
	private static GraphicsBuffer skyboxVbo;

	private final CubeMapTexture texture;
	private final ShaderProgram shader;

	public Skybox(CubeMapTexture texture, ShaderProgram shader) {
		this.texture = texture;
		this.shader = shader;

		this.shader.use();
		this.shader.setInt("skybox", 0);
		this.scale(1.f);
	}

	public static Result<Skybox, ShaderProgram.LinkageError> of(CubeMapTexture texture) {
		return ShaderProgram.builder()
				.shader(Shader.compile(ShaderType.FRAGMENT, SHADER_ID))
				.shader(Shader.compile(ShaderType.VERTEX, SHADER_ID))
				.withCleanup()
				.build()
				.then(shader -> Result.ok(new Skybox(texture, shader)));
	}

	/**
	 * Scales the skybox.
	 *
	 * @param scale the new scale of the skybox
	 */
	public void scale(float scale) {
		this.shader.use();
		this.shader.setFloat("scale", scale);
	}

	/**
	 * Draws the skybox.
	 */
	public void draw() {
		GL.get().depthFunc(GL.GL11.LEQUAL);
		this.shader.use();
		GL.get().activeTexture(GL.GL13.TEXTURE0);
		this.texture.bind();
		bindSkyboxVao();
		SKYBOX_VAO.draw(GeometricPrimitive.TRIANGLE_STRIP, 0, VAO_LAYOUT, VERTICES.length);
		SKYBOX_VAO.finish();
		this.texture.unbind();
		GL.get().depthFunc(GL.GL11.LESS);
	}

	@Override
	public void close() {
		this.shader.close();
		this.texture.close();
	}

	private static void bindSkyboxVao() {
		boolean valid = SKYBOX_VAO.isValid();

		SKYBOX_VAO.bind();

		if (!valid) {
			skyboxVbo = GraphicsBuffer.ofStatic(BufferTarget.ARRAY, VERTICES);
			SKYBOX_VAO.useLayout(VAO_LAYOUT);
			skyboxVbo.unbind();
		}
	}

	public static void terminate() {
		SKYBOX_VAO.close();
		if (skyboxVbo != null)
			skyboxVbo.close();
		skyboxVbo = null;
	}

	private static final float[] VERTICES = new float[]{
			// Positions
			// 1. Quad (complete)
			-1.f, -1.f, 1.f,
			-1.f, -1.f, -1.f,
			-1.f, 1.f, 1.f,
			-1.f, 1.f, -1.f,
			// 2. Quad
			1.f, 1.f, 1.f,
			1.f, 1.f, -1.f,
			// 3. Quad
			1.f, -1.f, 1.f,
			1.f, -1.f, -1.f,
			// | Loop 1 to stop rendering
			1.f, -1.f, -1.f,
			1.f, 1.f, -1.f,
			// | Loop 2 to reposition new start point
			// | 4. Quad (complete)
			1.f, 1.f, -1.f,
			-1.f, 1.f, -1.f,
			1.f, -1.f, -1.f,
			-1.f, -1.f, -1.f,
			// 5. Quad
			1.f, -1.f, 1.f,
			-1.f, -1.f, 1.f,
			// 6. Quad
			1.f, 1.f, 1.f,
			-1.f, 1.f, 1.f,
	};
}
