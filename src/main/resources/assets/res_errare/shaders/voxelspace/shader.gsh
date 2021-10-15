#version 330 core
layout (location = 0) in vec3 aPos;

layout (std140) uniform matrices {
	mat4 projection;
	mat4 view;
	mat4 ortho;
};

uniform int heightmap[];
uniform int colormap[];
uniform int screen_width;
uniform float scale = 1.f;

layout (line_strip, max_vertices = 2) out;

void draw_vertical_line(int x, float y0, float y1, vec3 color) {
	gl_Position = vec4(x, y0, 0, 0);
	EmitVertex();
	gl_Position = vec4(x, y1, 0, 0);
	EmitVertex();
	EndPrimitive();
}

int get_index(vec2 point) {
	int size = sqrt(heightmap.length());
	int x = int(point.x) % size;
	int y = int(point.y) % size;

	return x * (size * y) + x;
}

void render(vec2 position, float yaw, int height, int horizon, float scale_height, int distance, int screen_width, int screen_height) {
	// Precalculate viewing angle parameters.
	float sin_yaw = sin(yaw);
	float cos_yaw = cos(yaw);

	// Initialize visibility array. Y position for each column on screen.
	int y_buffer[screen_width];
	for (int i = 0; i < screen_width; i++) {
		y_buffer[i] = screen_height;
	}

	// Draw from front to the back (low Z coordinate to high Z coordinate)
	float z = 1.f, dz = 1.f;
	while (z < distance) {
		// Find line on map. This calculation corresponds to a field of view of 90°.
		vec2 left_point = vec2(
			(-cos_yaw * z - sin_yaw * z) + position.x,
			(sin_yaw * z - cos_yaw * z) + position.y
		);
		vec2 right_point = vec2(
			(cos_yaw * z - sin_yaw * z) + position.x,
			(-sin_yaw * z  - cos_yaw * z) + position.y
		);

		// Segment the line.
		float d_x = (right_point.x - left_point.x) / screen_width;
		float d_y = (right_point.y - left_point.y) / screen_width;

		// Raster line and draw vertical line for each segment.
		for (int i = 0; i < screen_width; i++) {
			float height_on_screen = (height - heightmap[get_index(left_point)]) / z * scale_height + horizon;

			draw_vertical_line(i, height_on_screen, y_buffer[i], colormap[get_index(left_point)]);

			if (height_on_screen < y_buffer[i]) {
				y_buffer[i] = int(height_on_screen);
			}

			left_point.x += d_x;
			right_point += d_y;
		}

		// Go to next line and increase step size when you are far away.
		z += dz;
		dz += .2f;
	}
}

void main() {
	vec2 pos = vec2(view[3][0], view[3][2]);
}
