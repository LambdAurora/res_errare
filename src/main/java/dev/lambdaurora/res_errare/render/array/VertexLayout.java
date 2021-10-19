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
import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.util.NativeSizes;
import jdk.incubator.foreign.MemoryAddress;

import java.util.List;

public record VertexLayout(List<Element> vertexElements) {
	public long vertexSize() {
		long size = 0;

		for (var element : this.vertexElements) {
			size += element.size();
		}

		return size;
	}

	public int vertexElementCount() {
		int count = 0;

		for (var element : this.vertexElements) {
			count += element.length();
		}

		return count;
	}

	public long offset(int index) {
		if (index >= this.vertexElements.size())
			return -1;

		long offset = 0;

		for (int i = 0; i < index; i++) {
			offset += this.vertexElements.get(i).size();
		}

		return offset;
	}

	public void applyAttrib() {
		for (int i = 0; i < this.vertexElements.size(); i++) {
			GL.get().enableVertexAttribArray(i);
			var elem = this.vertexElements.get(i);
			GL.get().vertexAttribPointer(i, elem.length(), GL.GL11.FLOAT, false, this.vertexSize(), MemoryAddress.ofLong(this.offset(i)));
		}
	}

	public static final Element VEC2F_ELEMENT = new Element(NativeSizes.VEC2_LENGTH, NativeSizes.VEC2F_SIZE);
	public static final Element VEC3F_ELEMENT = new Element(NativeSizes.VEC3_LENGTH, NativeSizes.VEC3F_SIZE);

	public record Element(int length, long size) {
	}
}
