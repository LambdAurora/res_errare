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

package dev.lambdaurora.res_errare.render.array;

import dev.lambdaurora.res_errare.render.GeometricPrimitive;
import dev.lambdaurora.res_errare.render.InvalidRenderingObjectException;
import dev.lambdaurora.res_errare.system.GL;

/**
 * Represents a vertex array object.
 */
public class VertexArray implements AutoCloseable {
	private int id;

	public VertexArray() {
	}

	public VertexArray(int id) {
		this.id = id;
	}

	public int id() {
		return this.id;
	}

	public boolean isValid() {
		return this.id != 0;
	}

	/**
	 * Binds this vertex array object.
	 */
	public void bind() {
		if (!this.isValid()) {
			this.id = GL.get().genVertexArrays(1)[0];

			if (!this.isValid())
				throw new InvalidRenderingObjectException("Could not generate a new vertex array object.");
		}

		GL.get().bindVertexArray(this.id);
	}

	public void finish() {
		unbind();
	}

	public void useLayout(VertexLayout layout) {
		this.bind();
		layout.applyAttrib();
	}

	public void draw(GeometricPrimitive primitive, int first, int count) {
		GL.get().drawArrays(primitive, first, count);
	}

	public void draw(GeometricPrimitive primitive, int first, VertexLayout layout, int rawLength) {
		GL.get().drawArrays(primitive, first, rawLength / layout.vertexElementCount());
	}

	@Override
	public void close() {
		if (this.isValid()) {
			GL.get().deleteVertexArrays(this.id);
			this.id = 0;
		}
	}

	public static void unbind() {
		GL.get().bindVertexArray(0);
	}
}
