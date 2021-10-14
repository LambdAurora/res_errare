#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

layout (std140) uniform matrices {
	mat4 projection;
	mat4 view;
};
uniform mat4 model;

out vec3 normal;
out vec2 texture_coords;

void main() {
	gl_Position = projection * view * model * vec4(aPos, 1.0);
	normal = aNormal;
	texture_coords = aTexCoords;
}
