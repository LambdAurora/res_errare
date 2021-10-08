package dev.lambdaurora.res_errare.render.texture;

import dev.lambdaurora.res_errare.system.GL;

/**
 * Represents a texture.
 */
public interface Texture {
	TextureType type();

	int id();

	/**
	 * Binds the texture for use.
	 */
	default void bind() {
		GL.get().bindTexture(this.type(), this.id());
	}

	/**
	 * Unbinds the texture.
	 */
	default void unbind() {
		unbind(this.type());
	}

	/**
	 * Deletes the texture. Once deleted the texture should not be used anymore.
	 */
	default void delete() {
		GL.get().deleteTextures(this.id());
	}

	static void unbind(TextureType type) {
		GL.get().bindTexture(type, 0);
	}
}
