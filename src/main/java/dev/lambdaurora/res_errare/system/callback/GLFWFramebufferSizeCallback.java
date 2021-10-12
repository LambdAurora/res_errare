package dev.lambdaurora.res_errare.system.callback;

import jdk.incubator.foreign.MemoryAddress;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@FunctionalInterface
public interface GLFWFramebufferSizeCallback {
	MethodHandle HANDLE = fetch();

	void onSetFramebufferSize(MemoryAddress window, int width, int height);

	private static MethodHandle fetch() {
		try {
			return MethodHandles.publicLookup().findVirtual(GLFWFramebufferSizeCallback.class, "onSetFramebufferSize",
					MethodType.methodType(void.class, MemoryAddress.class, int.class, int.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
}
