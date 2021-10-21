#version 330 core

#include res_errare:color.glsl

in vec2 texture_coords;

out vec4 FragColor;

uniform sampler2D screen_texture;

const float offset = 1.0 / 300.0;

void main() {
	vec4 color = texture(screen_texture, texture_coords);

	if (color.a > .9f) {
		FragColor = color;
		return;
	} else if (color.a == 0.f)
		discard;

	vec2 offsets[9] = vec2[](
		vec2(-offset,  offset), // top-left
		vec2( 0.0f,    offset), // top-center
		vec2( offset,  offset), // top-right
		vec2(-offset,  0.0f),   // center-left
		vec2( 0.0f,    0.0f),   // center-center
		vec2( offset,  0.0f),   // center-right
		vec2(-offset, -offset), // bottom-left
		vec2( 0.0f,   -offset), // bottom-center
		vec2( offset, -offset)  // bottom-right
	);

	float kernel[9] = float[](
		1.0 / 16, 2.0 / 16, 1.0 / 16,
		2.0 / 16, 4.0 / 16, 2.0 / 16,
		1.0 / 16, 2.0 / 16, 1.0 / 16
	);

	vec3 sample_texture[9];
	for(int i = 0; i < 9; i++) {
		sample_texture[i] = blend_colors(vec4(1.0, 1.0, 1.0, color.a), vec4(texture(screen_texture, texture_coords.st + offsets[i]).rgb, 1.0)).rgb;
	}
	vec3 col = vec3(0.0);
	for(int i = 0; i < 9; i++)
		col += sample_texture[i] * kernel[i];

	FragColor = vec4(col, 1.0);
}
