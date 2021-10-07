package dev.lambdaurora.res_errare.system;

import dev.lambdaurora.res_errare.render.ShaderType;
import jdk.incubator.foreign.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GL {
	private static GL self;
	private final Map<String, MethodHandle> functions = new HashMap<>();
	private final FunctionFetcher functionFetcher;

	private GL(FunctionFetcher functionFetcher) {
		this.functionFetcher = functionFetcher;
	}

	public static GL get() {
		if (self == null) {
			self = new GL(GLFW::getProcAddress);
		}

		return self;
	}

	private MethodHandle getFunction(String functionName, Function<MemoryAddress, MethodHandle> functionFactory) {
		return this.functions.computeIfAbsent(functionName, name -> functionFactory.apply(this.functionFetcher.fetch(name)));
	}

	/* GL 1.1 */

	public void clear(int mask) {
		try {
			this.getFunction("glClear", address -> CLinker.getInstance().downcallHandle(address,
							MethodType.methodType(void.class, int.class), FunctionDescriptor.ofVoid(CLinker.C_INT)))
					.invokeExact(mask);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void clearColor(float red, float green, float blue, float alpha) {
		try {
			this.getFunction("glClearColor", address -> CLinker.getInstance().downcallHandle(address,
							MethodType.methodType(void.class, float.class, float.class, float.class, float.class),
							FunctionDescriptor.ofVoid(CLinker.C_FLOAT, CLinker.C_FLOAT, CLinker.C_FLOAT, CLinker.C_FLOAT)))
					.invokeExact(red, green, blue, alpha);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	/* GL 2.0 */

	public int createShader(ShaderType type) {
		try {
			return (int) this.getFunction("glCreateShader", address -> CLinker.getInstance().downcallHandle(address,
							MethodType.methodType(int.class, int.class), FunctionDescriptor.of(CLinker.C_INT, CLinker.C_INT)))
					.invokeExact(type.glId());
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void deleteShader(int shader) {
		try {
			this.getFunction("glDeleteShader", address -> CLinker.getInstance().downcallHandle(address,
							MethodType.methodType(void.class, int.class), FunctionDescriptor.ofVoid(CLinker.C_INT)))
					.invokeExact(shader);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void shaderSource(int shader, String... source) {
		var function = this.getFunction("glShaderSource", address -> CLinker.getInstance().downcallHandle(address,
				MethodType.methodType(void.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class),
				FunctionDescriptor.ofVoid(CLinker.C_INT, CLinker.C_INT, CLinker.C_POINTER, CLinker.C_POINTER)));

		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);
			var cSource = new MemorySegment[source.length];

			for (int i = 0; i < source.length; i++)
				cSource[i] = CLinker.toCString(source[i], allocator);

			var sourceArray = allocator.allocateArray(MemoryLayouts.ADDRESS, cSource);
			function.invokeExact(shader, source.length, sourceArray.address(), MemoryAddress.NULL);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void compileShader(int shader) {
		try {
			this.getFunction("glCompileShader", address -> CLinker.getInstance().downcallHandle(address,
							MethodType.methodType(void.class, int.class), FunctionDescriptor.ofVoid(CLinker.C_INT)))
					.invokeExact(shader);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public int getShaderiv(int shader, int name) {
		var function = this.getFunction("glGetShaderiv", address -> CLinker.getInstance().downcallHandle(address,
				MethodType.methodType(void.class, int.class, int.class, MemoryAddress.class),
				FunctionDescriptor.ofVoid(CLinker.C_INT, CLinker.C_INT, CLinker.C_POINTER)));

		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var resultPtr = allocator.allocate(MemoryLayouts.JAVA_INT);

			function.invokeExact(shader, name, resultPtr.address());

			return resultPtr.toIntArray()[0];
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public String getShaderInfoLog(int shader, int maxLength) {
		var function = this.getFunction("glGetShaderInfoLog", address -> CLinker.getInstance().downcallHandle(address,
				MethodType.methodType(void.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class),
				FunctionDescriptor.ofVoid(CLinker.C_INT, CLinker.C_INT, CLinker.C_POINTER, CLinker.C_POINTER)));

		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var lengthSegment = allocator.allocate(MemoryLayouts.JAVA_INT);
			var logSegment = allocator.allocate(maxLength);

			function.invokeExact(shader, maxLength, lengthSegment.address(), logSegment.address());

			return CLinker.toJavaString(logSegment);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	@FunctionalInterface
	public interface FunctionFetcher {
		MemoryAddress fetch(String name);
	}

	public static class GL11 {
		public static final int GL_DEPTH_BUFFER_BIT = 0x100;
		public static final int GL_COLOR_BUFFER_BIT = 0x4000;
	}

	public static class GL20 {
		public static final int COMPILE_STATUS = 0x8b81;
		public static final int INFO_LOG_LENGTH = 0x8b84;
	}
}
