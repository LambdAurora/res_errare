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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

/**
 * Represents a 2D texture.
 */
public final class Texture2D implements Texture<Texture2D.Target> {
	private final int id;

	private Texture2D(int id) {
		this.id = id;
	}

	@Override
	public TextureType type() {
		return TextureType.TEXTURE_2D;
	}

	@Override
	public int id() {
		return this.id;
	}

	/**
	 * Uploads the given image to the texture.
	 *
	 * @param level the level
	 * @param image the image to upload
	 */
	public void upload(int level, Image image) {
		GL.get().texImage2D(this.type(), level, image.format().glInternalFormatId(), image);
	}

	/**
	 * {@return a new 2D texture builder}
	 *
	 * @param imageId the resource identifier of the image
	 */
	public static Builder builder(Identifier imageId) throws IOException {
		return builder(NativeImage.load(imageId, Image.Format.ARGB))
				.withCleanup();
	}

	/**
	 * {@return a new 2D texture builder}
	 *
	 * @param image the image uploaded to the texture
	 */
	public static Builder builder(@Nullable Image image) {
		return new Builder(image);
	}

	public enum Target implements OpenGLIdProvider {
		TEXTURE_2D(TextureType.TEXTURE_2D.glId());

		private final int glId;

		Target(int glId) {
			this.glId = glId;
		}

		@Override
		public int glId() {
			return this.glId;
		}
	}

	public static final class Builder {
		private final Map<TextureParameter<?>, Object> parameters = new Object2ObjectOpenHashMap<>();
		private Image image;
		private boolean cleanup = false;

		private Builder(@Nullable Image image) {
			this.image(image)
					.parameter(TextureParameters.MAG_FILTER, TextureParameters.FilterValue.LINEAR)
					.parameter(TextureParameters.MIN_FILTER, TextureParameters.FilterValue.LINEAR)
					.parameter(TextureParameters.WRAP_S, TextureParameters.WrapValue.CLAMP_TO_EDGE)
					.parameter(TextureParameters.WRAP_T, TextureParameters.WrapValue.CLAMP_TO_EDGE);
		}

		public Builder image(Identifier imageId) throws IOException {
			return this.image(NativeImage.load(imageId, Image.Format.ARGB))
					.withCleanup();
		}

		public Builder image(Image image) {
			this.image = image;
			return this;
		}

		public <V> Builder parameter(TextureParameter<V> parameter, V value) {
			this.parameters.put(parameter, value);
			return this;
		}

		public Builder withCleanup() {
			this.cleanup = true;
			return this;
		}

		public Texture2D build() {
			var texture = new Texture2D(GL.get().genTextures(1)[0]);
			texture.bind();

			if (this.image != null) {
				texture.upload(0, this.image);

				if (this.cleanup) {
					try {
						this.image.close();
					} catch (Exception e) {
						System.err.println("Could not close image.");
						e.printStackTrace();
					}
				}
			}

			//noinspection unchecked
			this.parameters.forEach((parameter, value) -> setParameters(texture, (TextureParameter<? super Object>) parameter, value));

			texture.unbind();
			return texture;
		}

		private static <T extends OpenGLIdProvider, V> void setParameters(Texture<T> texture, TextureParameter<V> param, V value) {
			texture.setParameter(param, value);
		}
	}
}
