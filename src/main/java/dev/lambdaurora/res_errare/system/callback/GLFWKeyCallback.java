package dev.lambdaurora.res_errare.system.callback;

import jdk.incubator.foreign.MemoryAddress;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public interface GLFWKeyCallback {
	MethodHandle HANDLE = fetch();

	void onKey(MemoryAddress window, int key, int scancode, int action, int mods);

	private static MethodHandle fetch() {
		try {
			return MethodHandles.publicLookup().findVirtual(GLFWKeyCallback.class, "onKey",
					MethodType.methodType(void.class, MemoryAddress.class, int.class, int.class, int.class, int.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
}
