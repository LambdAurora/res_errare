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

import jdk.incubator.foreign.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class LibraryLoader {
	private static final String ARCH = System.getProperty("os.arch");
	private static final List<String> LOADED_LIBRARIES = new ArrayList<>();
	private static final SymbolLookup SYMBOL_LOOKUP = SymbolLookup.loaderLookup();

	public static void loadLibrary(String name) {
		if (LOADED_LIBRARIES.contains(name))
			return;

		var path = lookForLibraryPath(name);

		System.out.println("LOAD LIBRARY FROM " + path);

		System.load(path.toString());
		LOADED_LIBRARIES.add(name);
	}

	public static Path lookForLibraryPath(String name) {
		if (Platform.get() != Platform.WINDOWS) {
			var libraryPath = Paths.get("/lib/" + System.mapLibraryName(name))
					.normalize();

			if (Files.exists(libraryPath)) {
				return libraryPath;
			}
		}

		var path = Paths.get("./lib/" + System.mapLibraryName(name + "-" + ARCH))
				.normalize().toAbsolutePath();

		if (!Files.exists(path)) throw new LinkageError("Could not find library " + name + ".");

		return path;
	}

	public static MemoryAddress lookupSymbol(String symbolName) {
		return SYMBOL_LOOKUP.lookup(symbolName).orElseThrow(() -> new LinkageError("Could not find symbol \"" + symbolName + "\""));
	}

	public static MethodHandle getFunctionHandle(MemoryAddress functionAddress, Class<?> returnType, Class<?>... params) {
		FunctionDescriptor descriptor;
		if (returnType == void.class) {
			descriptor = FunctionDescriptor.ofVoid(mapTypesToLayout(params));
		} else {
			descriptor = FunctionDescriptor.of(mapTypeToLayout(returnType), mapTypesToLayout(params));
		}

		return CLinker.getInstance().downcallHandle(functionAddress,
				MethodType.methodType(returnType, params),
				descriptor);
	}

	public static Function<MemoryAddress, MethodHandle> getNoArgFunctionProvider(Class<?> returnType) {
		return address -> getFunctionHandle(address, returnType);
	}

	public static MemoryLayout[] mapTypesToLayout(Class<?>... types) {
		var layouts = new MemoryLayout[types.length];

		for (int i = 0; i < types.length; i++)
			layouts[i] = mapTypeToLayout(types[i]);

		return layouts;
	}

	public static MemoryLayout mapTypeToLayout(Class<?> type) {
		if (type == int.class) {
			return CLinker.C_INT;
		} else if (type == long.class) {
			return CLinker.C_LONG;
		} else if (type == float.class) {
			return CLinker.C_FLOAT;
		} else if (Addressable.class.isAssignableFrom(type) || type == String.class) {
			return CLinker.C_POINTER;
		} else {
			throw new IllegalArgumentException("Cannot determine memory layout of type " + type + ".");
		}
	}
}
