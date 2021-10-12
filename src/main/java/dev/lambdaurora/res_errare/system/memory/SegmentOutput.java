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

package dev.lambdaurora.res_errare.system.memory;

import dev.lambdaurora.res_errare.util.NativeSizes;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectOutput;

public class SegmentOutput implements ObjectOutput {
	private final MemorySegment segment;
	private int offset = 0;

	public SegmentOutput(MemorySegment segment) {
		this.segment = segment;
	}

	@Override
	public void writeObject(Object obj) {
	}

	@Override
	public void write(int b) {
		this.writeByte(b);
	}

	@Override
	public void write(byte[] b) {
		this.write(b, 0, b.length);
	}

	@Override
	public void write(byte[] bytes, int off, int len) {
		for (byte b : bytes) {
			writeByte(b);
		}
	}

	@Override
	public void flush() throws IOException {

	}

	@Override
	public void close() throws IOException {

	}

	@Override
	public void writeBoolean(boolean v) {
		writeByte(v ? 1 : 0);
	}

	@Override
	public void writeByte(int v) {
		MemoryAccess.setByteAtOffset(this.segment, this.offset, (byte) v);
		this.offset++;
	}

	@Override
	public void writeShort(int v) {
		MemoryAccess.setShortAtOffset(this.segment, this.offset, (short) v);
		this.offset += 2;
	}

	@Override
	public void writeChar(int v) {
		MemoryAccess.setCharAtOffset(this.segment, this.offset, (char) v);
		this.offset += 2;
	}

	@Override
	public void writeInt(int v) {
		MemoryAccess.setIntAtOffset(this.segment, this.offset, v);
		this.offset += NativeSizes.INT_SIZE;
	}

	@Override
	public void writeLong(long v) {
		MemoryAccess.setLongAtOffset(this.segment, this.offset, v);
		this.offset += NativeSizes.LONG_SIZE;
	}

	@Override
	public void writeFloat(float v) {
		MemoryAccess.setFloatAtOffset(this.segment, this.offset, v);
		this.offset += NativeSizes.FLOAT_SIZE;
	}

	@Override
	public void writeDouble(double v) throws IOException {
		MemoryAccess.setDoubleAtOffset(this.segment, this.offset, v);
		this.offset += NativeSizes.DOUBLE_SIZE;
	}

	@Override
	public void writeBytes(@NotNull String s) throws IOException {

	}

	@Override
	public void writeChars(@NotNull String s) throws IOException {

	}

	@Override
	public void writeUTF(@NotNull String s) throws IOException {

	}
}
