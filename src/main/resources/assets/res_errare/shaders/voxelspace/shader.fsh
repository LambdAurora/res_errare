#version 330 core

in vec2 texture_coords;

out vec4 FragColor;

uniform sampler2D colormap_texture;

void main() {
	FragColor = texture(colormap_texture, texture_coords);
}
