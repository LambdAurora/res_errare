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

import dev.lambdaurora.res_errare.render.GeometricPrimitive;
import dev.lambdaurora.res_errare.render.buffer.BufferTarget;
import dev.lambdaurora.res_errare.render.buffer.BufferUsage;
import dev.lambdaurora.res_errare.render.buffer.range.Matrix4fBufferRange;
import dev.lambdaurora.res_errare.render.shader.ShaderType;
import dev.lambdaurora.res_errare.render.texture.Image;
import dev.lambdaurora.res_errare.render.texture.Texture;
import dev.lambdaurora.res_errare.render.texture.TextureType;
import dev.lambdaurora.res_errare.util.NativeSizes;
import jdk.incubator.foreign.*;
import org.joml.Matrix4f;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class GL {
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

	private void voidCallInt(String functionName, int param) {
		try {
			this.getFunction(functionName,
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class)
			).invokeExact(param);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	/* GL 1.1 */

	public void clear(int mask) {
		voidCallInt("glClear", mask);
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

	public void depthFunc(int func) {
		voidCallInt("glDepthFunc", func);
	}

	public void depthMask(boolean mask) {
		voidCallInt("glDepthMask", mask ? 1 : 0);
	}

	public void drawArrays(GeometricPrimitive mode, int first, int count) {
		try {
			this.getFunction("glDrawArrays", address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, int.class))
					.invokeExact(mode.glId(), first, count);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void enable(int capability) {
		voidCallInt("glEnable", capability);
	}

	public void disable(int capability) {
		voidCallInt("glDisable", capability);
	}

	public String getString(int name) {
		try {
			var res = (MemoryAddress) this.getFunction("glGetString",
							address -> LibraryLoader.getFunctionHandle(address, MemoryAddress.class, int.class))
					.invokeExact(name);

			return CLinker.toJavaString(res);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void polygonMode(int face, int mode) {
		try {
			this.getFunction("glPolygonMode", address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class))
					.invokeExact(face, mode);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void viewport(int x, int y, int width, int height) {
		try {
			this.getFunction("glViewport", address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, int.class, int.class))
					.invokeExact(x, y, width, height);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	/* 1.3 */

	public void activeTexture(int texture) {
		voidCallInt("glActiveTexture", texture);
	}

	/* 1.5 */

	public void bindBuffer(BufferTarget type, int vbo) {
		try {
			this.getFunction("glBindBuffer",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class)
			).invokeExact(type.glId(), vbo);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public int[] genBuffers(int n) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var cBuffers = allocator.allocateArray(CLinker.C_INT, n);

			this.getFunction("glGenBuffers",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, MemoryAddress.class)
			).invokeExact(n, cBuffers.address());

			return cBuffers.toIntArray();
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void deleteBuffers(int... buffers) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var cBuffers = allocator.allocateArray(CLinker.C_INT, buffers);

			this.getFunction("glDeleteBuffers",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, MemoryAddress.class)
			).invokeExact(buffers.length, cBuffers.address());
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void bufferData(BufferTarget type, float[] data, BufferUsage usage) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var cData = allocator.allocateArray(CLinker.C_FLOAT, data);

			bufferData(type, NativeSizes.sizeof(data), cData.address(), usage);
		}
	}

	public void bufferData(BufferTarget type, long size, MemoryAddress dataAddress, BufferUsage usage) {
		try {
			this.getFunction("glBufferData",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, long.class, MemoryAddress.class, int.class)
			).invokeExact(type.glId(), size, dataAddress, usage.glId());
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void bufferSubData(BufferTarget type, long offset, float[] data) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var cData = allocator.allocateArray(CLinker.C_FLOAT, data);

			bufferSubData(type, offset, NativeSizes.sizeof(data), cData.address());
		}
	}

	public void bufferSubData(BufferTarget type, long offset, long size, MemoryAddress dataAddress) {
		try {
			this.getFunction("glBufferSubData",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, long.class, long.class, MemoryAddress.class)
			).invokeExact(type.glId(), offset, size, dataAddress);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	/* GL 2.0 */

	public void bindTexture(TextureType type, int texture) {
		try {
			this.getFunction("glBindTexture",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class)
			).invokeExact(type.glId(), texture);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public int[] genTextures(int n) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var cTextures = allocator.allocateArray(CLinker.C_INT, n);

			this.getFunction("glGenTextures",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, MemoryAddress.class)
			).invokeExact(n, cTextures.address());

			return cTextures.toIntArray();
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void deleteTextures(int... textures) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var cTextures = allocator.allocateArray(CLinker.C_INT, textures);

			this.getFunction("glDeleteTextures",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, MemoryAddress.class)
			).invokeExact(textures.length, cTextures.address());
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void texImage2D(OpenGLIdProvider target, int level, Texture.InternalFormat internalFormat, Image image) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var imgData = image.getImageAddress(scope);

			this.texImage2D(target, level, internalFormat, image.width(), image.height(), image.format(), GL11.UNSIGNED_BYTE, imgData);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void texImage2D(OpenGLIdProvider target, int level, Texture.InternalFormat internalFormat,
	                       int width, int height, Image.Format format, int type, MemoryAddress data) {
		try {
			this.getFunction("glTexImage2D",
							address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, int.class, int.class,
									int.class, int.class, int.class, int.class, MemoryAddress.class))
					.invokeExact(target.glId(), level, internalFormat.glId(), width, height, 0, format.glFormatId(), type, data);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void texParameteri(TextureType target, OpenGLIdProvider paramName, int value) {
		try {
			this.getFunction("glTexParameteri", address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, int.class))
					.invokeExact(target.glId(), paramName.glId(), value);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void texParameterf(TextureType target, OpenGLIdProvider paramName, float value) {
		try {
			this.getFunction("glTexParameterf", address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, float.class))
					.invokeExact(target.glId(), paramName.glId(), value);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

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
		voidCallInt("glDeleteShader", shader);
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
		voidCallInt("glCompileShader", shader);
	}

	public int getShaderiv(int shader, int name) {
		var function = this.getFunction("glGetShaderiv",
				address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, MemoryAddress.class)
		);

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
		return this.getInfoLog(this.getFunction("glGetShaderInfoLog", address ->
				LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class)
		), shader, maxLength);
	}

	public int createProgram() {
		try {
			return (int) this.getFunction("glCreateProgram",
					address -> LibraryLoader.getFunctionHandle(address, int.class)
			).invokeExact();
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void deleteProgram(int program) {
		voidCallInt("glDeleteProgram", program);
	}

	public void attachShader(int program, int shader) {
		try {
			this.getFunction("glAttachShader",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class)
			).invokeExact(program, shader);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void detachShader(int program, int shader) {
		try {
			this.getFunction("glDetachShader",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class)
			).invokeExact(program, shader);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void linkProgram(int program) {
		voidCallInt("glLinkProgram", program);
	}

	public int getProgramiv(int program, int name) {
		var function = this.getFunction("glGetProgramiv",
				address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, MemoryAddress.class)
		);

		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var resultPtr = allocator.allocate(MemoryLayouts.JAVA_INT);

			function.invokeExact(program, name, resultPtr.address());

			return resultPtr.toIntArray()[0];
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public String getProgramInfoLog(int program, int maxLength) {
		return this.getInfoLog(this.getFunction("glGetProgramInfoLog", address ->
				LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class)
		), program, maxLength);
	}

	private String getInfoLog(MethodHandle function, int id, int maxLength) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var lengthSegment = allocator.allocate(MemoryLayouts.JAVA_INT);
			var logSegment = allocator.allocate(maxLength);

			function.invokeExact(id, maxLength, lengthSegment.address(), logSegment.address());

			return CLinker.toJavaString(logSegment);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void useProgram(int program) {
		voidCallInt("glUseProgram", program);
	}

	public int getUniformLocation(int program, String name) {
		try (var scope = ResourceScope.newConfinedScope()) {
			return (int) this.getFunction("glGetUniformLocation",
					address -> LibraryLoader.getFunctionHandle(address, int.class, int.class, MemoryAddress.class)
			).invokeExact(program, CLinker.toCString(name, scope).address());
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void uniform1i(int location, int value) {
		try {
			this.getFunction("glUniform1i",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class)
			).invokeExact(location, value);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void uniform1f(int location, float value) {
		try {
			this.getFunction("glUniform1f",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, float.class)
			).invokeExact(location, value);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void uniformMatrix4fv(int location, boolean transpose, Matrix4f value) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			this.getFunction("glUniformMatrix4fv",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, int.class, MemoryAddress.class)
			).invokeExact(location, 1, transpose ? 1 : 0, Matrix4fBufferRange.NO_OFFSET.createSegment(allocator, value).address());
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void enableVertexAttribArray(int index) {
		voidCallInt("glEnableVertexAttribArray", index);
	}

	public void vertexAttribPointer(int index, int size, int type, boolean normalized, long stride, MemoryAddress pointer) {
		try {
			this.getFunction("glVertexAttribPointer",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, int.class, int.class, int.class, MemoryAddress.class)
			).invokeExact(index, size, type, normalized ? 1 : 0, (int) stride, pointer);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	/* GL 3.0 */

	public void bindVertexArray(int vao) {
		voidCallInt("glBindVertexArray", vao);
	}

	public int[] genVertexArrays(int n) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var cVertexArrays = allocator.allocateArray(CLinker.C_INT, n);

			this.getFunction("glGenVertexArrays",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, MemoryAddress.class)
			).invokeExact(n, cVertexArrays.address());

			return cVertexArrays.toIntArray();
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void deleteVertexArrays(int... vertexArrays) {
		try (var scope = ResourceScope.newConfinedScope()) {
			var allocator = SegmentAllocator.ofScope(scope);

			var cVertexArrays = allocator.allocateArray(CLinker.C_INT, vertexArrays);

			this.getFunction("glDeleteVertexArrays",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, MemoryAddress.class)
			).invokeExact(vertexArrays.length, cVertexArrays.address());
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void bindBufferRange(BufferTarget target, int index, int buffer, long offset, long size) {
		try {
			this.getFunction("glBindBufferRange",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, int.class, long.class, long.class)
			).invokeExact(target.glId(), index, buffer, offset, size);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void generateMipmap(TextureType type) {
		voidCallInt("glGenerateMipmap", type.glId());
	}

	/* GL 4.2 */

	public void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, Access access, Texture.InternalFormat format) {
		try {
			this.getFunction("glBindImageTexture",
					address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class)
			).invokeExact(unit, texture, level, layered ? 1 : 0, layer, access.glId(), format.glId());
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void dispatchCompute(int numGroupsX, int numGroupsY, int numGroupsZ) {
		try {
			this.getFunction("glDispatchCompute", address -> LibraryLoader.getFunctionHandle(address, void.class, int.class, int.class, int.class))
					.invokeExact(numGroupsX, numGroupsY, numGroupsZ);
		} catch (Throwable e) {
			throw new NativeFunction.FunctionInvocationException(e);
		}
	}

	public void memoryBarrier(int barriers) {
		voidCallInt("glMemoryBarrier", barriers);
	}

	@FunctionalInterface
	public interface FunctionFetcher {
		MemoryAddress fetch(String name);
	}

	public enum Access implements OpenGLIdProvider {
		READ_ONLY(0x88b8),
		WRITE_ONLY(0x88b9),
		READ_WRITE(0x88ba);

		private final int glId;

		Access(int glId) {
			this.glId = glId;
		}

		@Override
		public int glId() {
			return this.glId;
		}
	}

	public static final class GL11 {
		public static final int DEPTH_BUFFER_BIT = 0x0100;
		public static final int LESS = 0x0201;
		public static final int EQUAL = 0x0202;
		public static final int LEQUAL = 0x0203;
		public static final int FRONT_AND_BACK = 0x0408;
		public static final int CULL_FACE = 0x0b44;
		public static final int DEPTH_TEST = 0x0b71;
		public static final int COLOR_BUFFER_BIT = 0x4000;
		public static final int UNSIGNED_BYTE = 0x1401;
		public static final int FLOAT = 0x1406;
		public static final int LINE = 0x1b01;
		public static final int FILL = 0x1b02;
		public static final int VENDOR = 0x1f00;
		public static final int RENDERER = 0x1f01;
	}

	public static final class GL13 {
		public static final int TEXTURE0 = 0x84c0;
		public static final int TEXTURE1 = 0x84c1;
	}

	public static final class GL20 {
		public static final int COMPILE_STATUS = 0x8b81;
		public static final int LINK_STATUS = 0x8b82;
		public static final int INFO_LOG_LENGTH = 0x8b84;
	}

	public static final class GL42 {
		public static final int SHADER_IMAGE_ACCESS_BARRIER_BIT = 0x00000020;
	}
}
