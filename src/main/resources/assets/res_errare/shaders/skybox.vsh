#version 330 core
layout (location = 0) in vec3 aPos;

out vec3 texture_coords;

layout (std140) uniform matrices {
	mat4 projection;
	mat4 view;
};

uniform float scale = 1.f;

void main() {
	texture_coords = vec3(aPos.xy, -aPos.z);
	// We need to remove the translation off of the view matrix.
	vec4 pos = projection * mat4(mat3(view)) * vec4(aPos * scale, 1.0);
	gl_Position = pos.xyww;
}
