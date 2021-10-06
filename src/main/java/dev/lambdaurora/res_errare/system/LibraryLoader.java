package dev.lambdaurora.res_errare.system;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LibraryLoader {
	private static final String ARCH = System.getProperty("os.arch");

	public static void loadLibrary(String name) {
		var path = lookForLibraryPath(name);

		System.out.println("LOAD LIBRARY FROM " + path);

		System.load(path.toString());
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
}
