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

package dev.lambdaurora.res_errare.render.buffer;

import dev.lambdaurora.res_errare.system.GL;
import jdk.incubator.foreign.MemoryAddress;

public final class GraphicsBuffer implements AutoCloseable {
	private final BufferTarget target;
	private final BufferUsage usage;
	private final BufferLayout layout;
	private final int id;

	private GraphicsBuffer(BufferTarget target, BufferUsage usage, BufferLayout layout, int id) {
		this.target = target;
		this.usage = usage;
		this.layout = layout;
		this.id = id;
	}

	public BufferTarget target() {
		return this.target;
	}

	public BufferUsage usage() {
		return this.usage;
	}

	public BufferLayout layout() {
		return this.layout;
	}

	public int id() {
		return this.id;
	}

	public void bind() {
		GL.get().bindBuffer(this.target, this.id);
	}

	public void unbind() {
		unbind(this.target);
	}

	public void reset() {
		this.bind();
		GL.get().bufferData(this.target, this.layout.size(), MemoryAddress.NULL, this.usage);
	}

	@Override
	public void close() {
		GL.get().deleteBuffers(this.id);
	}

	public static GraphicsBuffer of(BufferTarget target, BufferUsage usage, BufferLayout layout) {
		int id = GL.get().genBuffers(1)[0];
		return new GraphicsBuffer(target, usage, layout, id);
	}

	public static GraphicsBuffer ofStatic(BufferTarget target, float[] data) {
		var buffer = of(target, BufferUsage.STATIC_DRAW, BufferLayout.builder().addFloatArrayRange(data.length).build());
		buffer.bind();
		GL.get().bufferData(buffer.target(), data, buffer.usage());
		return buffer;
	}

	public static void unbind(BufferTarget target) {
		GL.get().bindBuffer(target, 0);
	}
}
