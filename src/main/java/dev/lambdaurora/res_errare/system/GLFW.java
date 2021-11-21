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

import dev.lambdaurora.res_errare.input.ButtonAction;
import dev.lambdaurora.res_errare.system.callback.GLFWFramebufferSizeCallback;
import dev.lambdaurora.res_errare.util.math.Dimensions2D;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import jdk.incubator.foreign.*;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.function.Function;

public final class GLFW {
	public static final int CONTEXT_VERSION_MAJOR = 0x00022002;
	public static final int CONTEXT_VERSION_MINOR = 0x00022003;
	public static final int OPENGL_PROFILE = 0x00022008;
	public static final int OPENGL_CORE_PROFILE = 0x00032001;

	private static final Map<String, MethodHandle> FUNCTIONS = new Object2ObjectOpenHashMap<>();
	// @TODO this is very stupid, needs to be stored with the window directly
	private static MemoryAddress currentFramebufferSizeCallback = MemoryAddress.NULL;

	public static void init() {
		LibraryLoader.loadLibrary("glfw");

		try {
			if ((int) getFunction("glfwInit", LibraryLoader.getNoArgFunctionProvider(int.class))
					.invokeExact() == 0)
				throw new RuntimeException(("Could not initialize GLFW."));
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	public static void terminate() {
		try {
			getFunction("glfwTerminate", LibraryLoader.getNoArgFunctionProvider(void.class))
					.invokeExact();
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	public static void pollEvents() {
		try {
			getFunction("glfwPollEvents", LibraryLoader.getNoArgFunctionProvider(void.class))
					.invokeExact();
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	private static MethodHandle getFunction(String name, Function<MemoryAddress, MethodHandle> linker) {
		return FUNCTIONS.computeIfAbsent(name, n -> linker.apply(LibraryLoader.lookupSymbol(n)));
	}

	public static float getTime() {
		try {
			return (float) getFunction("glfwGetTime", LibraryLoader.getNoArgFunctionProvider(float.class))
					.invokeExact();
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	/* Context stuff */

	public static MemoryAddress getProcAddress(String symbolName) {
		try (var scope = ResourceScope.newConfinedScope()) {
			return (MemoryAddress) getFunction("glfwGetProcAddress",
					address -> LibraryLoader.getFunctionHandle(address, MemoryAddress.class, MemoryAddress.class)
			).invokeExact(CLinker.toCString(symbolName, scope).address());
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	public static void swapInterval(int interval) {
		try {
			getFunction("glfwSwapInterval",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class)
			).invokeExact(interval);
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	/* Window stuff */

	public static void windowHint(int hint, int value) {
		try {
			getFunction("glfwWindowHint",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class)
			).invokeExact(hint, value);
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException("Could not invoke function glfwWindowHint: ", e);
		}
	}

	public static MemoryAddress createWindow(int width, int height, String title, MemoryAddress monitor, MemoryAddress share) {
		var function = getFunction("glfwCreateWindow", address -> LibraryLoader.getFunctionHandle(address,
				MemoryAddress.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class)
		);

		try (var scope = ResourceScope.newConfinedScope()) {
			var nativeTitle = CLinker.toCString(title, scope);
			return (MemoryAddress) function.invokeExact(width, height, nativeTitle.address(), monitor, share);
		} catch (Throwable throwable) {
			throw new NativeFunctionInvocationException("Could not invoke function glfwCreateWindow: ", throwable);
		}
	}

	public static void destroyWindow(MemoryAddress window) {
		try {
			getFunction("glfwDestroyWindow", address -> LibraryLoader.getFunctionHandle(address,
					void.class, MemoryAddress.class)
			).invokeExact(window);
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	public static void makeContextCurrent(MemoryAddress window) {
		try {
			getFunction("glfwMakeContextCurrent", address -> LibraryLoader.getFunctionHandle(address,
					void.class, MemoryAddress.class)
			).invokeExact(window);
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	public static boolean windowShouldClose(MemoryAddress address) {
		try {
			return (int) getFunction("glfwWindowShouldClose", addr -> LibraryLoader.getFunctionHandle(addr,
					int.class, MemoryAddress.class)
			).invokeExact(address) != 0;
		} catch (Throwable e) {
			return false;
		}
	}

	public static Dimensions2D getFramebufferSize(MemoryAddress window) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var widthSegment = allocator.allocate(CLinker.C_INT);
			var heightSegment = allocator.allocate(CLinker.C_INT);

			getFunction("glfwGetFramebufferSize", address -> LibraryLoader.getFunctionHandle(address, void.class,
					MemoryAddress.class, MemoryAddress.class, MemoryAddress.class)
			).invokeExact(window, widthSegment.address(), heightSegment.address());

			return new Dimensions2D(widthSegment.toIntArray()[0], heightSegment.toIntArray()[0]);
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	public static void setFramebufferSizeCallback(MemoryAddress window, GLFWFramebufferSizeCallback callback) {
		try {
			if (currentFramebufferSizeCallback != MemoryAddress.NULL) {
				CLinker.freeMemory(currentFramebufferSizeCallback);
			}

			currentFramebufferSizeCallback = CLinker.getInstance().upcallStub(
					GLFWFramebufferSizeCallback.HANDLE.bindTo(callback),
					FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT),
					ResourceScope.globalScope()
			);

			getFunction("glfwSetFramebufferSizeCallback", address -> LibraryLoader.getFunctionHandle(address, void.class,
					MemoryAddress.class, MemoryAddress.class)
			).invokeExact(window, currentFramebufferSizeCallback);
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	public static void swapBuffers(MemoryAddress window) {
		try {
			getFunction("glfwSwapBuffers", address -> LibraryLoader.getFunctionHandle(address,
					void.class, MemoryAddress.class)
			).invokeExact(window);
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	/* Input stuff */

	public static ButtonAction getKey(MemoryAddress window, int key) {
		try {
			return ButtonAction.byId(
					(int) getFunction("glfwGetKey", address -> LibraryLoader.getFunctionHandle(address,
							int.class, MemoryAddress.class, int.class)
					).invokeExact(window, key)
			);
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}

	public static void setKeyCallback(MemoryAddress window, MemoryAddress callback) {
		try {
			getFunction("glfwSetKeyCallback", address -> LibraryLoader.getFunctionHandle(address,
					void.class, MemoryAddress.class, MemoryAddress.class)
			).invokeExact(window, callback);
		} catch (Throwable e) {
			throw new NativeFunctionInvocationException(e);
		}
	}
}