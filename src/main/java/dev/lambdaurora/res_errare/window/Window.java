package dev.lambdaurora.res_errare.window;

import dev.lambdaurora.res_errare.system.GLFW;
import jdk.incubator.foreign.MemoryAddress;

import java.util.Optional;

public class Window {
	private final MemoryAddress handle;

	public Window(MemoryAddress handle) {
		this.handle = handle;
	}

	public void makeContextCurrent() {
		GLFW.makeContextCurrent(this.handle);
	}

	public boolean shouldClose() {
		return GLFW.windowShouldClose(this.handle);
	}

	/**
	 * Swaps the front and back buffers of the window.
	 * If the swap interval if greater than zero,
	 * the GPU driver waits the specified number of screen updates before swapping the buffers.
	 */
	public void swapBuffers() {
		GLFW.swapBuffers(this.handle);
	}

	public void destroy() {
		GLFW.destroyWindow(this.handle);
	}

	public static Optional<Window> create(int width, int height, String title) {
		var handle = GLFW.createWindow(width, height, title, MemoryAddress.NULL, MemoryAddress.NULL);
		if (handle.toRawLongValue() == 0)
			return Optional.empty();

		return Optional.of(new Window(handle));
	}
}
