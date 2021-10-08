package dev.lambdaurora.res_errare.resource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;

public class ResourceManager {
	private static ResourceManager resourceManager;
	private final FileSystem fileSystem;

	public ResourceManager(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	public static ResourceManager getDefault() {
		if (resourceManager == null) {
			try {
				resourceManager = new ResourceManager(getSelfFileSystem());
			} catch (URISyntaxException | IOException e) {
				throw new RuntimeException("Could not create default resource manager.", e);
			}
		}

		return resourceManager;
	}

	private static FileSystem getSelfFileSystem() throws URISyntaxException, IOException {
		return FileSystems.newFileSystem(
				Paths.get(ResourceManager.class.getProtectionDomain().getCodeSource().getLocation().toURI())
		);
	}
}
