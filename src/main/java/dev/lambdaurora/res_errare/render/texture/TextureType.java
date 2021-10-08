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
