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

import dev.lambdaurora.res_errare.parser.token.UnknownTokenException;

import java.util.Iterator;

/**
 * Represents the lexer of the calculator.
 * <p>
 * Takes a string and splits it into a series of {@linkplain ShaderToken}.
 */
public class ShaderLexer implements Iterator<ShaderToken> {
	// Cache the token types array to not rebuild it for each call.
	private static final ShaderTokenType[] TOKEN_TYPES = ShaderTokenType.values();

	private final String text;
	private int index;
	private int line = 0;
	private boolean startOfLine = true;

	private ShaderToken current;

	public ShaderLexer(String text) {
		this.text = text;
		this.index = 0;

		this.pickNext();
	}

	@Override
	public boolean hasNext() {
		return this.current.type() != ShaderTokenType.EOF;
	}

	@Override
	public ShaderToken next() {
		var current = this.current;
		this.pickNext();
		return current;
	}

	private void pickNext() {
		while (this.index < this.text.length()) {
			var part = this.text.substring(this.index);

			for (var type : TOKEN_TYPES) {
				var i = type.matcher().match(part, this.startOfLine);

				if (i > 0) {
					part = part.substring(0, i);
					this.current = new ShaderToken(type, part, this.line, this.index);
					this.index += i;

					for (int j = 0; j < part.length(); j++) {
						char c = part.charAt(j);

						if (c == '\n')
							this.line++;
						if (j == part.length() - 1) {
							this.startOfLine = c == '\n';
						}
					}
					return;
				} else if (i == -1) {
					throw new UnknownTokenException("Could not match token " + type + " since the token is incomplete.", this.index);
				}
			}

			if (!Character.isWhitespace(part.charAt(0))) {
				throw new UnknownTokenException("Unknown token start character \"" + part.charAt(0) + "\".", this.index);
			}

			this.index++;
		}

		this.current = new ShaderToken(ShaderTokenType.EOF, "", this.line, this.text.length());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}