#version 430

layout (local_size_x = 1, local_size_y = 1) in;

layout (rgba32f, binding = 0) uniform writeonly image2D img_output;
layout (rgba32f, binding = 1) uniform readonly image2D heightmap;
layout (rgba32f, binding = 2) uniform readonly image2D colormap;
uniform int height;
uniform int size;

layout (std140) uniform matrices {
	mat4 projection;
	mat4 view;
};

int get_tiling_texture_coord(int absolute_coord) {
	return absolute_coord % size;
}

ivec2 get_tiling_texture_coords(ivec2 absolute_coord) {
	return ivec2(get_tiling_texture_coord(absolute_coord.x), get_tiling_texture_coord(absolute_coord.y));
}

void draw_vertical_line(int x, int y0, int y1, vec4 color) {
	for (int i = y0; i <= y1; i++) {
		imageStore(img_output, ivec2(x, i), color);
	}
}

void main() {
	// gl_LocalInvocationID.xy * gl_WorkGroupID.xy == gl_GlobalInvocationID
	ivec2 coords = ivec2(gl_GlobalInvocationID);

	vec3 pos = vec3(view[3][0], view[3][1], view[3][2]);

	vec4 pixel;
	if (((gl_WorkGroupID.x & 1u) != 1u) != ((gl_WorkGroupID.y & 1u) == 1u)) {
		pixel = vec4(1.0, .5, .0, 1.0);
	} else {
		pixel = vec4(.0, .5, 1.0, 1.0);
	}

	imageStore(img_output, coords, pixel);
}
