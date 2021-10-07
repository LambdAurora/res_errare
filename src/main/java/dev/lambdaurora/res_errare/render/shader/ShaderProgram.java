package dev.lambdaurora.res_errare.render.shader;

import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.util.Result;

import java.util.ArrayList;
import java.util.List;

public record ShaderProgram(int id) {
	public void use() {
		GL.get().useProgram(this.id);
	}

	public void delete() {
		GL.get().deleteProgram(this.id);
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
					this.shaders.forEach(Shader::delete);

				return Result.fail(new LinkageError("Could not link shader program.", log));
			}

			this.shaders.forEach(shader -> GL.get().detachShader(id, shader.id()));

			if (this.cleanup)
				this.shaders.forEach(Shader::delete);

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
