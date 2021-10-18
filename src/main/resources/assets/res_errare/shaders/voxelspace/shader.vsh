#version 330 core

layout (location = 0) in vec4 aPos;

layout (std140) uniform matrices {
	mat4 projection;
	mat4 view;
	mat4 ortho;
};

out vec2 texture_coords;

void main() {
	int encoded_color = int(aPos.z);

	gl_Position = ortho * vec4(aPos.xy, 0.0, 1.0);
	texture_coords = aPos.zw;
}
