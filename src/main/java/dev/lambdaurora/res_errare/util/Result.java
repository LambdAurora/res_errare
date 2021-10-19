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

	/**
	 * {@return gets the result if successful, otherwise throw an exception}
	 */
	public T getOrThrow() {
		if (this.hasError()) {
			var error = this.getError();

			if (error instanceof RuntimeException runtimeException)
				throw runtimeException;
			else if (error instanceof Throwable throwable)
				throw new RuntimeException(throwable);
			else
				throw new RuntimeException(error.toString());
		}

		return this.get();
	}

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
