vec4 mix_colors(vec4 color1, vec4 color2) {
	return vec4(1, 1, 1, 1
	/*255 - SQRT(((255-Color1.R)^2 + (255-Color2.R)^2)/2),
	255 - SQRT(((255-Color1.G)^2 + (255-Color2.G)^2)/2),
	255 - SQRT(((255-Color1.B)^2 + (255-Color2.B)^2)/2),
	1*/
	);
}
