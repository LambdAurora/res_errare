package dev.lambdaurora.res_errare.render.shader.error;

import dev.lambdaurora.res_errare.render.shader.Shader;

public class ShaderPreprocessError extends Shader.CreationException {
	public ShaderPreprocessError(String message) {
		super(message);
	}

	public ShaderPreprocessError(String message, Throwable cause) {
		super(message, cause);
	}

	public ShaderPreprocessError(Throwable cause) {
		super(cause);
	}
}
