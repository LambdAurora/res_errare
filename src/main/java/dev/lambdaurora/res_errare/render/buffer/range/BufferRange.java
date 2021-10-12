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

package dev.lambdaurora.res_errare.render.buffer.range;

import dev.lambdaurora.res_errare.render.buffer.GraphicsBuffer;
import dev.lambdaurora.res_errare.system.GL;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;

public abstract class BufferRange<V> {
	private final long offset;
	private final long size;

	protected BufferRange(long offset, long size) {
		this.offset = offset;
		this.size = size;
	}

	public abstract Class<V> type();

	public long offset() {
		return this.offset;
	}

	public long size() {
		return this.size;
	}

	public void set(GraphicsBuffer buffer, V value) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var segment = this.createSegment(allocator, value);

			GL.get().bufferSubData(buffer.target(), this.offset(), this.size(), segment.address());
		}
	}

	protected abstract MemorySegment createSegment(SegmentAllocator allocator, V value);
}
