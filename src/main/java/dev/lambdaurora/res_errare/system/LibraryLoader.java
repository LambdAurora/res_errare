package dev.lambdaurora.res_errare.system;

import jdk.incubator.foreign.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
		} else if (Addressable.class.isAssignableFrom(type)) {
			return CLinker.C_POINTER;
		} else {
			throw new IllegalArgumentException("Cannot determine memory layout of type " + type + ".");
		}
	}
}
