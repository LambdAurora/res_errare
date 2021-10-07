package dev.lambdaurora.res_errare.util;

import java.util.function.Function;

/**
 * Represents a result which can error.
 *
 * @param <T> the type of the result
 * @param <E> the type of the error
 */
public abstract class Result<T, E> {
	public abstract T get();

	public abstract E getError();

	public abstract boolean hasError();

	public <N> Result<N, E> then(Function<T, Result<N, E>> thenFunction) {
		if (this.hasError())
			return fail(this.getError());
		else
			return thenFunction.apply(this.get());
	}

	public static <T, E> Result<T, E> ok(T result) {
		return new Result<>() {
			@Override
			public T get() {
				return result;
			}

			@Override
			public E getError() {
				return null;
			}

			@Override
			public boolean hasError() {
				return false;
			}
		};
	}

	public static <T, E> Result<T, E> fail(E error) {
		return new Result<>() {
			@Override
			public T get() {
				var error = this.getError();
				if (error instanceof Throwable throwable)
					throw new IllegalStateException(throwable);
				else throw new IllegalStateException(error.toString());
			}

			@Override
			public E getError() {
				return error;
			}

			@Override
			public boolean hasError() {
				return true;
			}
		};
	}
}
