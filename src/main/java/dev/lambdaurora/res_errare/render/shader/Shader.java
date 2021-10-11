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

import dev.lambdaurora.res_errare.resource.ResourceManager;
import dev.lambdaurora.res_errare.resource.ResourceType;
import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.util.Identifier;
import dev.lambdaurora.res_errare.util.Result;

import java.io.IOException;

/**
 * Represents an OpenGL shader.
 */
public record Shader(ShaderType type, int id) implements AutoCloseable {
	public static Result<Shader, CreationException> compile(ShaderType type, Identifier shaderId) {
		var resourceId = new Identifier(shaderId.namespace(), "shaders/" + shaderId.path() + '.' + type.extension());

		try {
			return compile(type, ResourceManager.getDefault(ResourceType.ASSETS).getStringFrom(resourceId));
		} catch (IOException e) {
			return Result.fail(new CreationException("Could not load shader " + shaderId + " of type " + type + ".", e));
		}
	}

	public static Result<Shader, CreationException> compile(ShaderType type, String source) {
		int id = GL.get().createShader(type);

		if (id == 0)
			return Result.fail(new CreationException("Could not create shader " + type + "."));

		GL.get().shaderSource(id, source);
		GL.get().compileShader(id);

		if (GL.get().getShaderiv(id, GL.GL20.COMPILE_STATUS) == 0) {
			int length = GL.get().getShaderiv(id, GL.GL20.INFO_LOG_LENGTH);
			var log = GL.get().getShaderInfoLog(id, length);

			GL.get().deleteShader(id);
			return Result.fail(new CompilationError("Could not compile shader " + type + ".", log));
		}

		return Result.ok(new Shader(type, id));
	}

	@Override
	public void close() {
		GL.get().deleteShader(this.id());
	}

	public static class CreationException extends RuntimeException {
		public CreationException() {
		}

		public CreationException(String message) {
			super(message);
		}

		public CreationException(String message, Throwable cause) {
			super(message, cause);
		}

		public CreationException(Throwable cause) {
			super(cause);
		}
	}

	public static class CompilationError extends CreationException {
		private final String log;

		public CompilationError(String log) {
			this.log = log;
		}

		public CompilationError(String message, String log) {
			super(message + " Log: " + log);
			this.log = log;
		}

		public CompilationError(String message, Throwable cause, String log) {
			super(message, cause);
			this.log = log;
		}

		public CompilationError(Throwable cause, String log) {
			super(cause);
			this.log = log;
		}

		public String getLog() {
			return this.log;
		}
	}
}
