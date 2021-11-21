#version 330 core

#include res_errare:color.glsl

in vec2 texture_coords;

out vec4 FragColor;

uniform sampler2D screen_texture;

void main() {
	FragColor = texture(screen_texture, texture_coords);
}
