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

	/**
	 * {@return the OpenGL identifier of this shader type}
	 */
	public int glId() {
		return this.glId;
	}
}
