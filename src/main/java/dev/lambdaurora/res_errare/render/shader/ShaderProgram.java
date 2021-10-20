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

package dev.lambdaurora.res_errare.render.shader;

import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.util.Result;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ShaderProgram(int id) implements AutoCloseable {
	public void use() {
		GL.get().useProgram(this.id);
	}

	private int getUniformLocation(String name) {
		return GL.get().getUniformLocation(this.id, name);
	}

	public void setBool(String name, boolean value) {
		this.setInt(name, value ? 1 : 0);
	}

	public void setInt(String name, int value) {
		GL.get().uniform1i(this.getUniformLocation(name), value);
	}

	public void setFloat(String name, float value) {
		GL.get().uniform1f(this.getUniformLocation(name), value);
	}

	public void setVec3f(String name, Vector3f value) {
		GL.get().uniform3f(this.getUniformLocation(name), value.x(), value.y(), value.z());
	}

	public void setMatrix4f(String name, Matrix4f value) {
		this.setMatrix4f(name, value, false);
	}

	public void setMatrix4f(String name, Matrix4f value, boolean transpose) {
		GL.get().uniformMatrix4fv(this.getUniformLocation(name), transpose, value);
	}

	/**
	 * Attaches a shader to this program.
	 * <p>
	 * The program needs to be {@link #link() linked} to apply any shader attachment.
	 *
	 * @param shader the shader to attach
	 */
	public void attachShader(Shader shader) {
		GL.get().attachShader(this.id(), shader.id());
	}

	/**
	 * Detaches a shader to this program.
	 * <p>
	 * The program needs to be {@link #link() linked} to apply any shader attachment.
	 *
	 * @param shader the shader to detach
	 */
	public void detachShader(Shader shader) {
		GL.get().detachShader(this.id(), shader.id());
	}

	/**
	 * Links the program.
	 *
	 * @return the error logs if the program couldn't be linked, otherwise empty
	 */
	public Optional<String> link() {
		GL.get().linkProgram(this.id());

		if (GL.get().getProgramiv(this.id(), GL.GL20.LINK_STATUS) == 0) {
			int length = GL.get().getProgramiv(this.id(), GL.GL20.INFO_LOG_LENGTH);
			var log = GL.get().getProgramInfoLog(this.id(), length);

			return Optional.of(log);
		}

		return Optional.empty();
	}

	public void dispatchCompute(int numGroupsX, int numGroupsY, int numGroupsZ) {
		GL.get().dispatchCompute(numGroupsX, numGroupsY, numGroupsZ);
	}

	@Override
	public void close() {
		GL.get().deleteProgram(this.id);
	}

	public static void useNone() {
		GL.get().useProgram(0);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final List<Shader> shaders = new ArrayList<>();
		private boolean cleanup = false;

		public Builder() {
		}

		public Builder shader(Shader shader) {
			this.shaders.add(shader);
			return this;
		}

		public Builder shader(Result<Shader, Shader.CreationException> shader) {
			return this.shader(shader.get());
		}

		public Builder withCleanup() {
			this.cleanup = true;
			return this;
		}

		public Result<ShaderProgram, LinkageError> build() {
			var program = new ShaderProgram(GL.get().createProgram());

			this.shaders.forEach(program::attachShader);

			var linkageResult = program.link();

			if (linkageResult.isPresent()) {
				program.close();

				if (this.cleanup)
					this.shaders.forEach(Shader::close);

				return Result.fail(new LinkageError("Could not link shader program.", linkageResult.get()));
			}

			if (this.cleanup) {
				this.shaders.forEach(shader -> {
					program.detachShader(shader);
					shader.close();
				});
			}

			return Result.ok(program);
		}
	}

	public static class LinkageError extends RuntimeException {
		private final String log;

		public LinkageError(String log) {
			this.log = log;
		}

		public LinkageError(String message, String log) {
			super(message + " Log: " + log);
			this.log = log;
		}

		public LinkageError(String message, Throwable cause, String log) {
			super(message, cause);
			this.log = log;
		}

		public LinkageError(Throwable cause, String log) {
			super(cause);
			this.log = log;
		}

		public String getLog() {
			return this.log;
		}
	}
}
