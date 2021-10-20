package dev.lambdaurora.res_errare.parser.token;

import dev.lambdaurora.res_errare.util.StringUtil;

import java.util.regex.Pattern;

/**
 * Represents a token matcher.
 * <p>
 * The goal is to have a simple way to match tokens.
 */
@FunctionalInterface
public interface TokenMatcher {
	/**
	 * Attempts to match a string.
	 *
	 * @param input the string to match
	 * @param startOfLine if the string to match is at the beginning of a line
	 * @return the length of the matched string, {@code 0} if the match failed.
	 */
	int match(String input, boolean startOfLine);

	/**
	 * Returns a single character token matcher.
	 *
	 * @param c the character to match
	 * @return the token matcher
	 */
	static TokenMatcher of(char c) {
		return of(c, true);
	}

	/**
	 * Returns a single character token matcher.
	 *
	 * @param c the character to match
	 * @param requireWhitespaceToSeparate {@code true} if the single character token matcher requires a whitespace character
	 * to separate the token, else {@code false}
	 * @return the token matcher
	 */
	static TokenMatcher of(char c, boolean requireWhitespaceToSeparate) {
		return (input, startOfLine) -> {
			if (input.charAt(0) == c) {
				if (requireWhitespaceToSeparate && input.length() > 1 && input.charAt(1) == c) return 0; // Might be a double character token.
				return 1;
			} else return 0;
		};
	}

	/**
	 * Returns a string token matcher.
	 *
	 * @param str the string to match
	 * @return the token matcher
	 */
	static TokenMatcher of(String str) {
		return (input, startOfLine) -> {
			if (input.startsWith(str)) return str.length();
			return 0;
		};
	}

	/**
	 * Returns a pattern-based token matcher.
	 *
	 * @param pattern the pattern to match against
	 * @return the token matcher
	 */
	static TokenMatcher of(Pattern pattern) {
		return (input, startOfLine) -> {
			var matcher = pattern.matcher(input);
			if (matcher.matches()) return matcher.end();
			else return 0;
		};
	}

	TokenMatcher CHAR = (input, startOfLine) -> {
		if (StringUtil.getCharAt(input, 0) != '\'')
			return 0;

		int i;
		boolean foundEnd = false;
		int shouldEnd = 0;
		for (i = 1; i < input.length() && !foundEnd; i++) {
			switch (input.charAt(i)) {
				case '\\' -> i++;
				case '\'' -> foundEnd = true;
				default -> shouldEnd = i + 1;
			}

			if (!foundEnd && i == shouldEnd)
				return -1;
		}

		if (!foundEnd)
			return -1;

		return i;
	};
	TokenMatcher STRING = (input, startOfLine) -> {
		if (StringUtil.getCharAt(input, 0) != '"')
			return 0;

		int i;
		boolean foundEnd = false;
		for (i = 1; i < input.length() && !foundEnd; i++) {
			switch (input.charAt(i)) {
				case '\\' -> i++;
				case '"' -> foundEnd = true;
			}
		}

		if (!foundEnd)
			return -1;

		return i;
	};
}
