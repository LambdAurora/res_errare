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

package dev.lambdaurora.res_errare.util;

import org.lwjgl.system.MemoryUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public enum ResourceUtil {
	;

	public static ByteBuffer readResource(InputStream inputStream) throws IOException {
		ByteBuffer buffer;

		if (inputStream instanceof FileInputStream fileInputStream) {
			FileChannel fileChannel = fileInputStream.getChannel();
			buffer = MemoryUtil.memAlloc((int) fileChannel.size() + 1);

			//noinspection StatementWithEmptyBody
			while (fileChannel.read(buffer) != -1) ;
		} else {
			buffer = MemoryUtil.memAlloc(8192);
			ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);

			while (readableByteChannel.read(buffer) != -1) {
				if (buffer.remaining() == 0) {
					buffer = MemoryUtil.memRealloc(buffer, buffer.capacity() * 2);
				}
			}
		}

		return buffer;
	}
}
