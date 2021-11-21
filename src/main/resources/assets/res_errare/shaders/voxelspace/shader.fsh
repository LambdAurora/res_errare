#version 330 core

#include res_errare:color.glsl

in vec2 texture_coords;

out vec4 FragColor;

uniform sampler2D screen_texture;

const vec4 BACKGROUND_COLOR = vec4(0.7, 0.8, 0.9, 1.0);
const float offset = 1.0 / 300.0;

void main() {
	vec4 color = texture(screen_texture, texture_coords);

	if (color.a > .9f) {
		FragColor = color;
		return;
	} else if (color.a == 0.f) {
		FragColor = BACKGROUND_COLOR;
		return;
	}

	FragColor = blend_colors(vec4(BACKGROUND_COLOR.rgb, color.a), vec4(texture(screen_texture, texture_coords).rgb, 1));
}
