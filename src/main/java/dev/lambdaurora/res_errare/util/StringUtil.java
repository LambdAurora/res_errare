package dev.lambdaurora.res_errare.util;

/**
 * Contains utilities for manipulating strings.
 */
public enum StringUtil {
	;

	public static char getCharAt(String string, int index) {
		if (index < string.length())
			return string.charAt(index);
		return 0;
	}
}
