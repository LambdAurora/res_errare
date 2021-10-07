package dev.lambdaurora.res_errare.render;

import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.util.Result;

/**
 * Represents an OpenGL shader.
 */
public record Shader(ShaderType type, int id) {
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

	public void delete() {
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
