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

package dev.lambdaurora.res_errare.window;

import dev.lambdaurora.res_errare.input.ButtonAction;
import dev.lambdaurora.res_errare.system.GLFW;
import dev.lambdaurora.res_errare.system.callback.GLFWKeyCallback;
import dev.lambdaurora.res_errare.util.math.Dimensions2D;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.ResourceScope;

import java.util.Optional;

/**
 * Represents a window.
 */
public class Window {
	private final MemoryAddress handle;
	private MemoryAddress currentKeyCallback = MemoryAddress.NULL;

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
		freeIfNeeded(this.currentKeyCallback);
	}

	public Dimensions2D getFramebufferSize() {
		return GLFW.getFramebufferSize(this.handle);
	}

	public void setFramebufferSizeCallback(FramebufferSizeCallback callback) {
		GLFW.setFramebufferSizeCallback(this.handle, (window, width, height) -> callback.onSetFramebufferSize(width, height));
	}

	/* Input */

	public ButtonAction getKey(int key) {
		return GLFW.getKey(this.handle, key);
	}

	public void setKeyCallback(KeyCallback callback) {
		freeIfNeeded(this.currentKeyCallback);

		this.currentKeyCallback = CLinker.getInstance().upcallStub(
				GLFWKeyCallback.HANDLE.bindTo((GLFWKeyCallback) (window, key, scancode, action, mods)
						-> callback.onKey(key, scancode, ButtonAction.byId(action), mods)),
				FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT, CLinker.C_INT, CLinker.C_INT),
				ResourceScope.globalScope()
		);

		GLFW.setKeyCallback(this.handle, this.currentKeyCallback);
	}

	private static void freeIfNeeded(MemoryAddress address) {
		if (address != MemoryAddress.NULL)
			CLinker.freeMemory(address);
	}

	public static Optional<Window> create(int width, int height, String title) {
		var handle = GLFW.createWindow(width, height, title, MemoryAddress.NULL, MemoryAddress.NULL);
		if (handle.toRawLongValue() == 0)
			return Optional.empty();

		return Optional.of(new Window(handle));
	}

	@FunctionalInterface
	public interface FramebufferSizeCallback {
		void onSetFramebufferSize(int width, int height);
	}

	@FunctionalInterface
	public interface KeyCallback {
		void onKey(int key, int scancode, ButtonAction action, int mods);
	}
}
