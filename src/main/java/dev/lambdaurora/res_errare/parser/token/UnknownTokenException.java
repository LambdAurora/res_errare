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

package dev.lambdaurora.res_errare.parser.token;

/**
 * Represents an unknown token exception.
 * <p>
 * Thrown when the lexer encounters a non-whitespace character and cannot associate it with a token type.
 */
public class UnknownTokenException extends RuntimeException {
	private final int errorOffset;

	public UnknownTokenException(String message, int errorOffset) {
		super(message);
		this.errorOffset = errorOffset;
	}

	/**
	 * Gets the offset in the input string where the error happened.
	 *
	 * @return the error offset
	 */
	public int getErrorOffset() {
		return this.errorOffset;
	}
}
