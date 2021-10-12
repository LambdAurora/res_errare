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

import dev.lambdaurora.res_errare.util.NativeSizes;
import jdk.incubator.foreign.*;

public class FloatArrayBufferRange extends BufferRange<float[]> {
	public FloatArrayBufferRange(long offset, long length) {
		super(offset, length * NativeSizes.FLOAT_SIZE);
	}

	@Override
	public Class<float[]> type() {
		return float[].class;
	}

	@Override
	protected MemorySegment createSegment(SegmentAllocator allocator, float[] value) {
		return allocator.allocateArray(CLinker.C_FLOAT, value);
	}
}
