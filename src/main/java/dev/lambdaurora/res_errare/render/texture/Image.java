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

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.ResourceScope;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an image.
 */
public interface Image extends AutoCloseable {
	Format format();

	int width();

	int height();

	@Nullable MemoryAddress getImageAddress(ResourceScope scope);

	enum Format {
		RED(1, 0x1903),
		RGB(3, 0x1907),
		ARGB(4, 0x1908);

		private final int channelCount;
		private final int glFormatId;
		private final int glInternalId;

		Format(int channelCount, int glId) {
			this(channelCount, glId, glId);
		}

		Format(int channelCount, int glFormatId, int glInternalId) {
			this.channelCount = channelCount;
			this.glFormatId = glFormatId;
			this.glInternalId = glInternalId;
		}

		public int channelCount() {
			return this.channelCount;
		}

		public int glFormatId() {
			return this.glFormatId;
		}

		public int glInternalFormatId() {
			return this.glInternalId;
		}
	}
}
