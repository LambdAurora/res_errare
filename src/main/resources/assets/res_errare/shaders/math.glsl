float distance_with(vec2 first, vec2 second) {
	float x = second.x - first.x;
	float y = second.y - first.y;
	return sqrt(x * x + y * y);
}
