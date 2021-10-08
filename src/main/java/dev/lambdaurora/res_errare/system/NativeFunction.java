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

package dev.lambdaurora.res_errare.system;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public record NativeFunction<T>(MethodHandle handle) {
	@SuppressWarnings("unchecked")
	public T invoke() {
		try {
			return (T) this.handle.invoke();
		} catch (Throwable e) {
			throw new FunctionInvocationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public T invoke(Object... params) {
		try {
			return (T) this.handle.invoke(params);
		} catch (Throwable e) {
			throw new FunctionInvocationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public T invokeExact(Object... params) {
		try {
			return (T) this.handle.invokeExact(params);
		} catch (Throwable e) {
			throw new FunctionInvocationException(e);
		}
	}

	public static NativeFunction<Void> ofVoid(String name) {
		return of(name, void.class, FunctionDescriptor.ofVoid());
	}

	public static <T> NativeFunction<T> of(String name, Class<? extends T> returnType,
	                                       FunctionDescriptor descriptor) {
		return of(LibraryLoader.lookupSymbol(name), returnType, new Class<?>[0], descriptor);
	}

	public static <T> NativeFunction<T> of(String name, Class<? extends T> returnType, Class<?>[] parameters,
	                                       FunctionDescriptor descriptor) {
		return of(LibraryLoader.lookupSymbol(name), returnType, parameters, descriptor);
	}

	public static <T> NativeFunction<T> of(MemoryAddress symbolAddress, Class<? extends T> returnType,
	                                       FunctionDescriptor descriptor) {
		return of(symbolAddress, returnType, new Class<?>[0], descriptor);
	}

	public static <T> NativeFunction<T> of(MemoryAddress symbolAddress, Class<? extends T> returnType, Class<?>[] parameters,
	                                       FunctionDescriptor descriptor) {
		return new NativeFunction<>(CLinker.getInstance().downcallHandle(
				symbolAddress,
				MethodType.methodType(returnType, parameters),
				descriptor
		));
	}

	public static class FunctionInvocationException extends RuntimeException {
		public FunctionInvocationException() {
		}

		public FunctionInvocationException(String message) {
			super(message);
		}

		public FunctionInvocationException(String message, Throwable cause) {
			super(message, cause);
		}

		public FunctionInvocationException(Throwable cause) {
			super(cause);
		}

		public FunctionInvocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}
}
