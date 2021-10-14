#version 330 core

in vec3 normal;
in vec2 texture_coords;

out vec4 FragColor;

uniform sampler2D texture_diffuse1;
uniform sampler2D texture_normal1;
uniform sampler2D texture_specular1;

void main() {
	vec4 texture_color = texture(texture_diffuse1, texture_coords);
	if (texture_color.a < 0.1)
		discard;
	FragColor = texture_color;
}
