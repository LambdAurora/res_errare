/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
