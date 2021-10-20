package dev.lambdaurora.res_errare.render.shader.parser;

import dev.lambdaurora.res_errare.parser.token.TokenMatcher;
import dev.lambdaurora.res_errare.util.StringUtil;

public enum ShaderTokenType {
	DIRECTIVE((input, startOfLine) -> {
		if (!startOfLine || StringUtil.getCharAt(input, 0) != '#')
			return 0;

		int i;
		for (i = 1; i < input.length(); i++) {
			char c = input.charAt(i);

			if (c == '\\') {
				i++;
			} else if (c == '\n')
				break;
		}

		return i;
	}),
	CHAR(TokenMatcher.CHAR),
	STRING(TokenMatcher.STRING),
	EOF(TokenMatcher.of('\0')),
	RAW((input, startOfLine) -> {
		int i;

		for (i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c == '"' || c == '\'')
				break;

			if (c == '\n' && StringUtil.getCharAt(input, i + 1) == '#') {
				i++;
				break;
			}
		}

		return i;
	});

	private final TokenMatcher matcher;

	ShaderTokenType(TokenMatcher matcher) {
		this.matcher = matcher;
	}

	public TokenMatcher matcher() {
		return this.matcher;
	}
}
