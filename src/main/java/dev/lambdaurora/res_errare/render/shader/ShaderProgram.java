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

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public void close() {
		GL.get().deleteProgram(this.id);
	}

	public static void useNone() {
		GL.get().useProgram(0);
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
			int id = GL.get().createProgram();

			this.shaders.forEach(shader -> GL.get().attachShader(id, shader.id()));

			GL.get().linkProgram(id);

			if (GL.get().getProgramiv(id, GL.GL20.LINK_STATUS) == 0) {
				int length = GL.get().getProgramiv(id, GL.GL20.INFO_LOG_LENGTH);
				var log = GL.get().getProgramInfoLog(id, length);

				GL.get().deleteProgram(id);

				if (this.cleanup)
					this.shaders.forEach(Shader::close);

				return Result.fail(new LinkageError("Could not link shader program.", log));
			}

			this.shaders.forEach(shader -> GL.get().detachShader(id, shader.id()));

			if (this.cleanup)
				this.shaders.forEach(Shader::close);

			return Result.ok(new ShaderProgram(id));
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
