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

import dev.lambdaurora.res_errare.system.GL;
import dev.lambdaurora.res_errare.system.OpenGLIdProvider;
import dev.lambdaurora.res_errare.util.Identifier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a cube map texture.
 */
public final class CubeMapTexture implements Texture<CubeMapTexture.CubeMapTextureTarget> {
	private final int id;

	private CubeMapTexture(int id) {
		this.id = id;
	}

	@Override
	public TextureType type() {
		return TextureType.TEXTURE_CUBE_MAP;
	}

	@Override
	public int id() {
		return this.id;
	}

	/**
	 * {@return a new cube map texture builder}
	 */
	public static Builder builder() {
		return new Builder();
	}

	public enum CubeMapTextureTarget implements OpenGLIdProvider {
		POSITIVE_X(0x8515),
		NEGATIVE_X(0x8516),
		POSITIVE_Y(0x8517),
		NEGATIVE_Y(0x8518),
		POSITIVE_Z(0x8519),
		NEGATIVE_Z(0x851a);

		private final int glId;

		CubeMapTextureTarget(int glId) {
			this.glId = glId;
		}

		@Override
		public int glId() {
			return this.glId;
		}
	}

	public static final class Builder {
		private final Map<CubeMapTextureTarget, Image> faces = new EnumMap<>(CubeMapTextureTarget.class);
		private final Map<TextureParameter<?>, Object> parameters = new Object2ObjectOpenHashMap<>();

		private Builder() {
			this.parameter(TextureParameters.MAG_FILTER, TextureParameters.FilterValue.LINEAR)
					.parameter(TextureParameters.MIN_FILTER, TextureParameters.FilterValue.LINEAR)
					.parameter(TextureParameters.WRAP_S, TextureParameters.WrapValue.CLAMP_TO_EDGE)
					.parameter(TextureParameters.WRAP_T, TextureParameters.WrapValue.CLAMP_TO_EDGE)
					.parameter(TextureParameters.WRAP_R, TextureParameters.WrapValue.CLAMP_TO_EDGE);
		}

		public Builder face(CubeMapTextureTarget target, Image image) {
			this.faces.put(target, image);
			return this;
		}

		public Builder face(CubeMapTextureTarget target, Identifier imageId) throws IOException {
			return this.face(target, NativeImage.load(imageId, Image.Format.ARGB));
		}

		public <V> Builder parameter(TextureParameter<V> parameter, V value) {
			this.parameters.put(parameter, value);
			return this;
		}

		public CubeMapTexture build() {
			if (this.faces.size() != 6) {
				throw new IllegalArgumentException("Cannot build cube map texture with an incomplete set of images.");
			}

			var texture = new CubeMapTexture(GL.get().genTextures(1)[0]);
			texture.bind();

			//noinspection unchecked
			this.parameters.forEach((parameter, value) -> setParameters(texture, (TextureParameter<? super Object>) parameter, value));

			for (var entry : this.faces.entrySet()) {
				texture.upload(entry.getKey(), 0, entry.getValue());
			}

			texture.unbind();
			return texture;
		}

		private static <T extends OpenGLIdProvider, V> void setParameters(Texture<T> texture, TextureParameter<V> param, V value) {
			texture.setParameter(param, value);
		}
	}
}
