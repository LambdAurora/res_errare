#version 330 core
layout (location = 0) in vec3 aPos;

layout (std140) uniform matrices {
    mat4 projection;
    mat4 view;
};

uniform float scale = 1.f;

out vec3 texture_coords;

void main()
{
    texture_coords = aPos;
    vec4 pos = projection * view * vec4(aPos * scale, 1.0);
    gl_Position = pos.xyww;
}
