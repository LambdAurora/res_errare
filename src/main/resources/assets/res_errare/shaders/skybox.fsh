#version 330 core

in vec3 texture_coords;

out vec4 FragColor;

uniform samplerCube skybox;

void main() {
    FragColor = texture(skybox, texture_coords);
}
