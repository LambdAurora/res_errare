package dev.lambdaurora.res_errare.render.texture;

public class BaseTexture implements Texture {
	private final TextureType type;
	private final int id;

	public BaseTexture(TextureType type, int id) {
		this.type = type;
		this.id = id;
	}

	@Override
	public TextureType type() {
		return this.type;
	}

	@Override
	public int id() {
		return this.id;
	}
}
