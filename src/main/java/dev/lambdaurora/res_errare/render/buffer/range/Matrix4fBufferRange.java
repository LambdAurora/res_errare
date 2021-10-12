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

import dev.lambdaurora.res_errare.system.memory.SegmentOutput;
import dev.lambdaurora.res_errare.util.NativeSizes;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SegmentAllocator;
import org.joml.Matrix4f;

import java.io.IOException;

public class Matrix4fBufferRange extends BufferRange<Matrix4f> {
	public Matrix4fBufferRange(long offset) {
		super(offset, NativeSizes.MATRIX4F_SIZE);
	}

	@Override
	public Class<Matrix4f> type() {
		return Matrix4f.class;
	}

	@Override
	public MemorySegment createSegment(SegmentAllocator allocator, Matrix4f value) {
		var segment = allocator.allocate(this.size());
		try {
			value.writeExternal(new SegmentOutput(segment));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return segment;
	}
}
