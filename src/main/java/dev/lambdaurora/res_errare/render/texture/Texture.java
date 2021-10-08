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
