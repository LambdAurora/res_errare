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

import dev.lambdaurora.res_errare.system.OpenGLIdProvider;

/**
 * Represents a shader type.
 */
public enum ShaderType implements OpenGLIdProvider {
	COMPUTE("csh", 0x91b9),
	FRAGMENT("fsh", 0x8b30),
	@Deprecated
	GEOMETRY("gsh", 0x8dd9),
	VERTEX("vsh", 0x8b31);

	private final String extension;
	private final int glId;

	ShaderType(String extension, int glId) {
		this.extension = extension;
		this.glId = glId;
	}

	/**
	 * {@return the file extension of this shader type}
	 */
	public String extension() {
		return this.extension;
	}

	@Override
	public int glId() {
		return this.glId;
	}
}
