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

package dev.lambdaurora.res_errare.render.texture;

import dev.lambdaurora.res_errare.resource.ResourceManager;
import dev.lambdaurora.res_errare.resource.ResourceType;
import dev.lambdaurora.res_errare.util.Identifier;
import dev.lambdaurora.res_errare.util.ResourceUtil;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.ResourceScope;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class NativeImage implements Image {
	private final Format format;
	private final int width;
	private final int height;
	private final long pointer;

	private NativeImage(Format format, int width, int height, long pointer) {
		this.format = format;
		this.width = width;
		this.height = height;
		this.pointer = pointer;
	}

	public static NativeImage load(Identifier resourceId, Format format) throws IOException {
		return load(ResourceManager.getDefault(ResourceType.ASSETS).open(resourceId), format);
	}

	public static NativeImage load(InputStream stream, Format format) throws IOException {
		ByteBuffer inputBuffer = null;

		try {
			inputBuffer = ResourceUtil.readResource(stream);
			inputBuffer.rewind();
			return load(inputBuffer, format);
		} finally {
			MemoryUtil.memFree(inputBuffer);

			if (stream != null) {
				try {
					stream.close();
				} catch (IOException ignored) {
					// Ignored.
				}
			}
		}
	}

	public static NativeImage load(ByteBuffer inputBuffer, Format format) throws IOException {
		if (MemoryUtil.memAddress(inputBuffer) == 0) {
			throw new IllegalArgumentException("Invalid buffer.");
		}

		var memStack = MemoryStack.stackPush();
		try {
			var width = new int[1];
			var height = new int[1];
			var channels = new int[1];
			var imageBuffer = STBImage.stbi_load_from_memory(
					inputBuffer, width, height, channels, format.channelCount()
			);

			if (imageBuffer == null) {
				throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
			}

			return new NativeImage(format, width[0], height[0], MemoryUtil.memAddress(imageBuffer));
		} catch (Throwable e) {
			try {
				memStack.close();
			} catch (Throwable otherError) {
				e.addSuppressed(otherError);
			}

			throw e;
		} finally {
			memStack.close();
		}
	}

	@Override
	public Format format() {
		return this.format;
	}

	@Override
	public int width() {
		return this.width;
	}

	@Override
	public int height() {
		return this.height;
	}

	@Override
	public MemoryAddress getImageAddress(ResourceScope scope) {
		return MemoryAddress.ofLong(this.pointer);
	}

	@Override
	public void close() {
		MemoryUtil.nmemFree(this.pointer);
	}
}
