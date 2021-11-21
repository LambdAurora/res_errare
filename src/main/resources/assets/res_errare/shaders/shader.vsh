#version 330 core

layout (location = 0) in vec3 i_pos;
layout (location = 1) in vec3 i_normal;
layout (location = 2) in vec2 i_texture_coords;

out vec3 normal;
out vec2 texture_coords;

#include res_errare:common_matrices.glsl
uniform mat4 model;

void main() {
	gl_Position = projection * view * model * vec4(i_pos, 1.0);
	normal = i_normal;
	texture_coords = i_texture_coords;
}
