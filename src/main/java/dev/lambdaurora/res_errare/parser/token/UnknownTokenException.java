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
