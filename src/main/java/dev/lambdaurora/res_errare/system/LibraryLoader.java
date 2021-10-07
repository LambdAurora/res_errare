package dev.lambdaurora.res_errare.system;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.SymbolLookup;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LibraryLoader {
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
}
