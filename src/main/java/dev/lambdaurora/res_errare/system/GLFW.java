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

import dev.lambdaurora.res_errare.util.math.Dimensions2D;
import jdk.incubator.foreign.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class GLFW {
	private static final Map<String, NativeFunction<?>> FUNCTIONS = new HashMap<>();

	public static void init() {
		LibraryLoader.loadLibrary("glfw");

		if (NativeFunction.of("glfwInit", int.class, FunctionDescriptor.of(CLinker.C_INT))
				.invoke() == 0)
			throw new RuntimeException(("Could not initialize GLFW."));
	}

	public static void terminate() {
		NativeFunction.ofVoid("glfwTerminate").invoke();
	}

	public static void pollEvents() {
		NativeFunction.ofVoid("glfwPollEvents").invoke();
	}

	@SuppressWarnings("unchecked")
	private static <T> NativeFunction<T> getFunction(String name, Function<String, NativeFunction<T>> linker) {
		return (NativeFunction<T>) FUNCTIONS.computeIfAbsent(name, linker);
	}

	public static MemoryAddress getProcAddress(String symbolName) {
		try (var scope = ResourceScope.newConfinedScope()) {
			return (MemoryAddress) getFunction("glfwGetProcAddress", name -> NativeFunction.of(name,
					MemoryAddress.class, new Class[]{MemoryAddress.class}, FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER)))
					.handle().invoke(CLinker.toCString(symbolName, scope).address());
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public static MemoryAddress createWindow(int width, int height, String title, MemoryAddress monitor, MemoryAddress share) {
		var function = getFunction("glfwCreateWindow", name -> NativeFunction.of(name,
				MemoryAddress.class, new Class[]{int.class, int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class},
				FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT, CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_POINTER)
		));

		try (var scope = ResourceScope.newConfinedScope()) {
			var nativeTitle = CLinker.toCString(title, scope);
			return (MemoryAddress) function.handle().invoke(width, height, nativeTitle.address(), monitor, share);
		} catch (Throwable throwable) {
			throw new NativeFunction.FunctionInvocationException("Could not invoke function glfwCreateWindow: ", throwable);
		}
	}

	public static void destroyWindow(MemoryAddress window) {
		try {
			getFunction("glfwDestroyWindow", name -> NativeFunction.of(name,
					void.class, new Class[]{MemoryAddress.class}, FunctionDescriptor.ofVoid(CLinker.C_POINTER)))
					.handle().invoke(window);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public static void makeContextCurrent(MemoryAddress window) {
		try {
			getFunction("glfwMakeContextCurrent", name -> NativeFunction.of(name,
					void.class, new Class[]{MemoryAddress.class}, FunctionDescriptor.ofVoid(CLinker.C_POINTER)))
					.handle().invoke(window);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public static boolean windowShouldClose(MemoryAddress address) {
		try {
			return (int) getFunction("glfwWindowShouldClose", name -> NativeFunction.of(name,
					int.class, new Class[]{MemoryAddress.class}, FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER)))
					.handle().invoke(address) != 0;
		} catch (Throwable e) {
			return false;
		}
	}

	public static Dimensions2D getFramebufferSize(MemoryAddress window) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var widthSegment = allocator.allocate(CLinker.C_INT);
			var heightSegment = allocator.allocate(CLinker.C_INT);

			getFunction("glfwGetFramebufferSize", name -> NativeFunction.of(name, void.class,
					new Class[]{MemoryAddress.class, MemoryAddress.class, MemoryAddress.class},
					FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_POINTER)))
					.handle().invoke(window, widthSegment.address(), heightSegment.address());

			return new Dimensions2D(widthSegment.toIntArray()[0], heightSegment.toIntArray()[0]);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public static void swapBuffers(MemoryAddress window) {
		try {
			getFunction("glfwSwapBuffers", name -> NativeFunction.of(name,
					void.class, new Class[]{MemoryAddress.class}, FunctionDescriptor.ofVoid(CLinker.C_POINTER)))
					.handle().invoke(window);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}
}