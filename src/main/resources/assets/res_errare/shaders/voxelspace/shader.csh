#version 430

#include res_errare:math.glsl

layout (local_size_x = 1, local_size_y = 1) in;

layout (rgba32f, binding = 0) uniform writeonly image2D img_output;
layout (rgba32f, binding = 1) uniform readonly image2D heightmap;
layout (rgba32f, binding = 2) uniform readonly image2D colormap;
uniform int size;
uniform vec3 pos;
uniform float yaw;
uniform float pitch;

const float render_distance = 2000;

int get_tiling_texture_coord(int absolute_coord) {
	return absolute_coord % size;
}

ivec2 get_tiling_texture_coords(ivec2 absolute_coord) {
	return ivec2(get_tiling_texture_coord(absolute_coord.x), get_tiling_texture_coord(absolute_coord.y));
}

void draw_vertical_line(int x, int y0, int y1, ivec2 size, vec4 color) {
	y0 = size.y - y0;
	y1 = size.y - y1;
	for (int i = y1; i <= y0; i++) {
		imageStore(img_output, ivec2(x, i), color);
	}
}

void main() {
	// gl_LocalInvocationID.xy * gl_WorkGroupID.xy == gl_GlobalInvocationID
	ivec2 coords = ivec2(gl_GlobalInvocationID);
	ivec2 screen_dimensions = imageSize(img_output);

	draw_vertical_line(coords.x, 0, screen_dimensions.y, screen_dimensions, vec4(0.1f, 0.3f, 1.f, 0.f));

	float pos_x = pos.z;
	float pos_z = -pos.x;

	float horizon = 15;
	float scale_height = 240;

	// Precalculate viewing angle parameters.
	float sin_yaw = sin(yaw);
	float cos_yaw = cos(yaw);

	// Initialize visibility array. Y position for each column on screen.
	int y_buffer = screen_dimensions.y;

	float fog_length = render_distance - 850;

	// Draw from front to the back (low Z coordinate to high Z coordinate)
	float z = 1.f, dz = 1.f;
	while (z < render_distance) {
		// Find line on map. This calculation corresponds to a field of view of 90°.
		vec2 left_point = vec2(
			-cos_yaw * z - sin_yaw * z,
			sin_yaw * z - cos_yaw * z
		);
		vec2 right_point = vec2(
			cos_yaw * z - sin_yaw * z,
			-sin_yaw * z  - cos_yaw * z
		);

		// Segment the line.
		float d_x = (right_point.x - left_point.x) / screen_dimensions.x;
		float d_y = (right_point.y - left_point.y) / screen_dimensions.x;

		left_point.x += pos_x + d_x * coords.x;
		left_point.y += pos_z + d_y * coords.x;

		// Raster line and draw vertical line for each segment.
		float height_on_screen = (pos.y - imageLoad(heightmap, get_tiling_texture_coords(ivec2(left_point))).x * 255) / z * scale_height + pitch;

		vec4 color = imageLoad(colormap, get_tiling_texture_coords(ivec2(left_point)));

		float distance_from_camera = distance_with(vec2(pos_x, pos_z), left_point);
		if (distance_from_camera > 850) {
			color.a = min((distance_from_camera - 850) / fog_length, .9f);
		}

		draw_vertical_line(coords.x, int(height_on_screen), y_buffer, screen_dimensions, color);

		if (height_on_screen < y_buffer) {
			y_buffer = int(height_on_screen);
		}

		// Go to next line and increase step size when you are far away.
		z += dz;
		dz += .005f;
	}
}
