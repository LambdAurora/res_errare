package dev.lambdaurora.res_errare.util;

/**
 * Represents an exception fired if an {@link Identifier} is invalid.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class InvalidIdentifierException extends RuntimeException {
	public InvalidIdentifierException(String message) {
		super(message);
	}

	public InvalidIdentifierException(String message, Throwable cause) {
		super(message, cause);
	}
}
