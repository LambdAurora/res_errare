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

package dev.lambdaurora.res_errare.render.texture;

import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.system.OpenGLIdProvider;
import jdk.incubator.foreign.MemoryAddress;

/**
 * Represents a texture.
 *
 * @param <T> the accepted texture targets
 */
public interface Texture<T extends OpenGLIdProvider> extends AutoCloseable {
	TextureType type();

	/**
	 * {@return the OpenGL identifier of this texture}
	 */
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
	 * Uploads the given image to the texture.
	 *
	 * @param target the texture target
	 * @param level the level
	 * @param internalFormat the internal format used by OpenGL for this texture
	 * @param width the width of the texture
	 * @param height the height of the texture
	 */
	default void initEmpty(T target, int level, InternalFormat internalFormat, int width, int height) {
		this.bind();
		GL.get().texImage2D(target, level, internalFormat, width, height, Image.Format.ARGB, internalFormat.typeId(), MemoryAddress.NULL);
	}

	/**
	 * Uploads the given image to the texture.
	 *
	 * @param target the texture target
	 * @param level the level
	 * @param image the image to upload
	 */
	default void upload(T target, int level, Image image) {
		this.upload(target, level, image.format().internalFormat(), image);
	}

	/**
	 * Uploads the given image to the texture.
	 *
	 * @param target the texture target
	 * @param level the level
	 * @param internalFormat the internal format used by OpenGL for this texture
	 * @param image the image to upload
	 */
	default void upload(T target, int level, InternalFormat internalFormat, Image image) {
		GL.get().texImage2D(target, level, internalFormat, image);
	}

	default BoundImageTexture<T> bindImageTexture(int unit, int level, GL.Access access, InternalFormat format) {
		GL.get().bindImageTexture(unit, this.id(), level, false, 0, access, format);
		return new BoundImageTexture<>(unit, this, access, format);
	}

	default BoundImageTexture<T> bindImageTexture(int unit, int level, int layer, GL.Access access, InternalFormat format) {
		GL.get().bindImageTexture(unit, this.id(), level, true, layer, access, format);
		return new BoundImageTexture<>(unit, this, access, format);
	}

	default <V> void setParameter(TextureParameter<V> parameter, V value) {
		switch (parameter.type()) {
			case INTEGER -> GL.get().texParameteri(this.type(), parameter, parameter.getIntValue(value));
			case FLOAT -> GL.get().texParameterf(this.type(), parameter, parameter.getFloatValue(value));
		}
	}

	default void generateMipmap() {
		GL.get().generateMipmap(this.type());
	}

	/**
	 * Deletes the texture. Once deleted the texture should not be used anymore.
	 */
	@Override
	default void close() {
		GL.get().deleteTextures(this.id());
	}

	static void unbind(TextureType type) {
		GL.get().bindTexture(type, 0);
	}

	enum InternalFormat implements OpenGLIdProvider {
		R8UI(0x8232, GL.GL11.UNSIGNED_BYTE),
		R32F(0x822e, GL.GL11.FLOAT),
		RGB8UI(0x8d7d, GL.GL11.UNSIGNED_BYTE),
		RGB32F(0x8815, GL.GL11.FLOAT),
		RGBA8UI(0x8d7c, GL.GL11.UNSIGNED_BYTE),
		RGBA32F(0x8814, GL.GL11.FLOAT);

		private final int glId;
		private final int typeId;

		InternalFormat(int glId, int typeId) {
			this.glId = glId;
			this.typeId = typeId;
		}

		@Override
		public int glId() {
			return this.glId;
		}

		public int typeId() {
			return this.typeId;
		}
	}
}
