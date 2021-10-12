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

import dev.lambdaurora.res_errare.render.buffer.range.BufferRange;
import dev.lambdaurora.res_errare.render.buffer.range.FloatArrayBufferRange;
import dev.lambdaurora.res_errare.render.buffer.range.Matrix4fBufferRange;

import java.util.ArrayList;
import java.util.List;

public final class BufferLayout {
	private static final BufferLayout EMPTY = new BufferLayout(List.of());
	private final List<BufferRange<?>> ranges;
	private final long size;

	public BufferLayout(List<BufferRange<?>> ranges) {
		this.ranges = ranges;
		this.size = totalSize(ranges);
	}

	public <V> BufferRange<V> get(int index) {
		//noinspection unchecked
		return (BufferRange<V>) this.ranges.get(index);
	}

	public <V> BufferRange<V> getFirstOf(Class<V> type) {
		for (var range : this.ranges) {
			if (type.isAssignableFrom(range.type()))
				//noinspection unchecked
				return (BufferRange<V>) range;
		}

		return null;
	}

	public long size() {
		return this.size;
	}

	public static BufferLayout empty() {
		return EMPTY;
	}

	public static Builder builder() {
		return new Builder();
	}

	private static long totalSize(List<BufferRange<?>> ranges) {
		long size = 0;

		for (var range : ranges) {
			size += range.size();
		}

		return size;
	}

	public static class Builder {
		private final List<BufferRange<?>> ranges = new ArrayList<>();
		private int offset;

		private void addRange(BufferRange<?> range) {
			this.ranges.add(range);
			this.offset += range.size();
		}

		public Builder addFloatArrayRange(long length) {
			this.addRange(new FloatArrayBufferRange(this.offset, length));
			return this;
		}

		public Builder addMatrix4fRange() {
			this.addRange(new Matrix4fBufferRange(this.offset));
			return this;
		}

		public BufferLayout build() {
			return new BufferLayout(this.ranges);
		}
	}
}
