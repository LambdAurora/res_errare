package dev.lambdaurora.res_errare.render.texture;

/**
 * Represents the available texture types.
 */
public enum TextureType {
	TEXTURE_1D(0x0de0),
	TEXTURE_2D(0x0de1),
	TEXTURE_CUBE_MAP(0x8513);

	private final int glId;

	TextureType(int glId) {
		this.glId = glId;
	}

	/**
	 * {@return the OpenGL identifier of this texture type}
	 */
	public int glId() {
		return this.glId;
	}
}
