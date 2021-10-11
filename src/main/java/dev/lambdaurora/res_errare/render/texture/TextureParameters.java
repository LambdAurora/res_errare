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

import dev.lambdaurora.res_errare.system.OpenGLIdProvider;

/**
 * Contains various definitions of texture parameters.
 */
public enum TextureParameters {
	;

	public static final TextureParameter<FilterValue> MAG_FILTER = fromEnum(0x2800);
	public static final TextureParameter<FilterValue> MIN_FILTER = fromEnum(0x2801);

	public static final TextureParameter<WrapValue> WRAP_S = fromEnum(0x2802);
	public static final TextureParameter<WrapValue> WRAP_T = fromEnum(0x2803);
	public static final TextureParameter<WrapValue> WRAP_R = fromEnum(0x8072);

	private static <V extends OpenGLIdProvider> TextureParameter<V> fromEnum(int id) {
		return new TextureParameter<>() {
			@Override
			public int glId() {
				return id;
			}

			@Override
			public OpenGLInputType type() {
				return OpenGLInputType.INTEGER;
			}

			@Override
			public int getIntValue(V input) {
				return input.glId();
			}

			@Override
			public float getFloatValue(V input) {
				return input.glId();
			}
		};
	}

	public enum FilterValue implements OpenGLIdProvider {
		LINEAR(0x2601);

		private final int glId;

		FilterValue(int glId) {
			this.glId = glId;
		}

		@Override
		public int glId() {
			return this.glId;
		}
	}

	public enum WrapValue implements OpenGLIdProvider {
		CLAMP_TO_EDGE(0x812f),
		CLAMP_TO_BORDER(0x812d),
		MIRRORED_REPEAT(0x8370),
		REPEAT(0x2901),
		MIRROR_CLAMP_TO_EDGE(0x8743);

		private final int glId;

		WrapValue(int glId) {
			this.glId = glId;
		}

		@Override
		public int glId() {
			return this.glId;
		}
	}
}
