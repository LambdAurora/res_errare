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
import dev.lambdaurora.res_errare.render.buffer.BufferTarget;
import dev.lambdaurora.res_errare.render.buffer.GraphicsBuffer;
import dev.lambdaurora.res_errare.render.shader.Shader;
import dev.lambdaurora.res_errare.render.shader.ShaderProgram;
import dev.lambdaurora.res_errare.render.shader.ShaderType;
import dev.lambdaurora.res_errare.render.texture.CubeMapTexture;
import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.util.Identifier;
import dev.lambdaurora.res_errare.util.NativeSizes;
import dev.lambdaurora.res_errare.util.Result;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;

/**
 * Represents a skybox.
 */
public class Skybox implements AutoCloseable {
	public static final Identifier SKYBOX_SHADER_ID = new Identifier(Constants.NAMESPACE, "skybox");
	private static int skyboxVao;
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
		return new ShaderProgram.Builder()
				.shader(Shader.compile(ShaderType.FRAGMENT, SKYBOX_SHADER_ID))
				.shader(Shader.compile(ShaderType.VERTEX, SKYBOX_SHADER_ID))
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
		bindSkyboxVao();
		GL.get().activeTexture(GL.GL13.TEXTURE0);
		this.texture.bind();
		GL.get().drawArrays(GeometricPrimitive.TRIANGLES, 0, 36);
		GL.get().bindVertexArray(0);
		GL.get().depthFunc(GL.GL11.LESS);
	}

	@Override
	public void close() {
		this.shader.close();
		this.texture.close();
	}

	private static void bindSkyboxVao() {
		if (skyboxVao == 0) {
			skyboxVao = GL.get().genVertexArrays(1)[0];
			GL.get().bindVertexArray(skyboxVao);
			skyboxVbo = GraphicsBuffer.ofStatic(BufferTarget.ARRAY, VERTICES);
			GL.get().enableVertexAttribArray(0);
			GL.get().vertexAttribPointer(0, 3, GL.GL11.FLOAT, false, NativeSizes.FLOAT_SIZE * 3, MemoryAddress.NULL);
			skyboxVbo.unbind();
		}

		GL.get().bindVertexArray(skyboxVao);
	}

	public static void terminate() {
		GL.get().deleteVertexArrays(skyboxVao);
		skyboxVbo.close();
		skyboxVao = 0;
		skyboxVbo = null;
	}

	private static final float[] VERTICES = new float[]{
			// Positions
			-1.f, 1.f, -1.f,
			-1.f, -1.f, -1.f,
			1.f, -1.f, -1.f,
			1.f, -1.f, -1.f,
			1.f, 1.f, -1.f,
			-1.f, 1.f, -1.f,
			-1.f, -1.f, 1.f,
			-1.f, -1.f, -1.f,
			-1.f, 1.f, -1.f,
			-1.f, 1.f, -1.f,
			-1.f, 1.f, 1.f,
			-1.f, -1.f, 1.f,
			1.f, -1.f, -1.f,
			1.f, -1.f, 1.f,
			1.f, 1.f, 1.f,
			1.f, 1.f, 1.f,
			1.f, 1.f, -1.f,
			1.f, -1.f, -1.f,
			-1.f, -1.f, 1.f,
			-1.f, 1.f, 1.f,
			1.f, 1.f, 1.f,
			1.f, 1.f, 1.f,
			1.f, -1.f, 1.f,
			-1.f, -1.f, 1.f,
			-1.f, 1.f, -1.f,
			1.f, 1.f, -1.f,
			1.f, 1.f, 1.f,
			1.f, 1.f, 1.f,
			-1.f, 1.f, 1.f,
			-1.f, 1.f, -1.f,
			-1.f, -1.f, -1.f,
			-1.f, -1.f, 1.f,
			1.f, -1.f, -1.f,
			1.f, -1.f, -1.f,
			-1.f, -1.f, 1.f,
			1.f, -1.f, 1.f
	};
}
