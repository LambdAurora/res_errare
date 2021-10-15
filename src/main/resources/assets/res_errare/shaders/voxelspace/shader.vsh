#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aTexCoords;

layout (std140) uniform matrices {
	mat4 projection;
	mat4 view;
	mat4 ortho;
};

out vec3 color;

void main() {
	gl_Position = ortho * vec4(aPos, 1.0);
	color = vec3(1, 1, 1);
}
