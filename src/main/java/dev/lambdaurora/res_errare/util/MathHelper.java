package dev.lambdaurora.res_errare.util;

public class MathHelper {
	public static float lerp(float v0, float v1, float t) {
		return (1 - t) * v0 + t * v1;
	}
}
