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
	 * Uploads the given image to the texture?
	 *
	 * @param target the texture target
	 * @param level the level
	 * @param image the image to upload
	 */
	default void upload(T target, int level, Image image) {
		GL.get().texImage2D(target, level, Image.Format.ARGB.glInternalFormatId(), image);
	}

	default <V> void setParameter(TextureParameter<V> parameter, V value) {
		switch (parameter.type()) {
			case INTEGER -> GL.get().texParameteri(this.type(), parameter, parameter.getIntValue(value));
			case FLOAT -> GL.get().texParameterf(this.type(), parameter, parameter.getFloatValue(value));
		}
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
}
