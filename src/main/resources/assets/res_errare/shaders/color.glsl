vec4 blend_colors(vec4 fg, vec4 bg) {
	vec4 color;
	color.a = 1 - (1 - fg.a) * (1 - bg.a);
	if (color.a < 1.0e-6) return color; // Fully transparent -- r,g,b not important
	color.r = fg.r * fg.a / color.a + bg.r * bg.a * (1 - fg.a) / color.a;
	color.g = fg.g * fg.a / color.a + bg.g * bg.a * (1 - fg.a) / color.a;
	color.b = fg.b * fg.a / color.a + bg.b * bg.a * (1 - fg.a) / color.a;
	
	return color;
}

vec4 mix_colors(vec4 first, vec4 second) {
	return vec4(
		(255 - sqrt((pow(255 - first.r * 255.f, 2) + pow(255 - second.r * 255.f, 2)) / 2)) / 255.f,
		(255 - sqrt((pow(255 - first.g * 255.f, 2) + pow(255 - second.g * 255.f, 2)) / 2)) / 255.f,
		(255 - sqrt((pow(255 - first.b * 255.f, 2) + pow(255 - second.b * 255.f, 2)) / 2)) / 255.f,
		1
	);
}

float max_rgb(vec4 color) {
	float max = color.r;
	if (max < color.g)
	max = color.g;
	if (max < color.b)
	max = color.b;
	return max;
}

vec4 distribute_rgb(vec4 color) {
	float threshold = 0.999f;
	float m = max_rgb(color);
	if (m <= threshold)
	return color;

	float total = color.r + color.g + color.b;
	if (total >= 3 * threshold)
	return vec4(threshold, threshold, threshold, 1.f);

	float x = (3.f * threshold - total) / (3.f * m - total);
	float gray = threshold - x * m;
	return vec4(gray + x * color.r, gray + x * color.g, gray + x * color.b, 1.f);
}
