package dev.lambdaurora.res_errare.render.shader;

/**
 * Represents a shader type.
 */
public enum ShaderType {
	FRAGMENT(0x8b30),
	GEOMETRY(0x8dd9),
	VERTEX(0x8b31);

	private final int glId;

	ShaderType(int glId) {
		this.glId = glId;
	}

	public int glId() {
		return this.glId;
	}
}
