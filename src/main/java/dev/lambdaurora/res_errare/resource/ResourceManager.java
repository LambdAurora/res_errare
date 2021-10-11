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

package dev.lambdaurora.res_errare.resource;

import dev.lambdaurora.res_errare.Constants;
import dev.lambdaurora.res_errare.util.Identifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class ResourceManager {
	private static final Map<ResourceType, ResourceManager> RESOURCE_MANAGERS = new EnumMap<>(ResourceType.class);
	private final ResourceType type;
	private final Path root;
	private final String separator;

	public ResourceManager(ResourceType type, Path root) {
		this.type = type;
		this.root = root;
		this.separator = this.root.getFileSystem().getSeparator();
	}

	private Path getPath(String path) {
		return this.root.resolve(path.replace("/", this.separator))
				.toAbsolutePath().normalize();
	}

	private Path getPath(Identifier resourceId) {
		return this.getPath(type.directory() + '/' + resourceId.namespace() + '/' + resourceId.path());
	}

	public boolean contains(Identifier resourceId) {
		return Files.exists(this.getPath(resourceId));
	}

	public String getStringFrom(Identifier resourceId) throws IOException {
		try {
			return Files.readString(this.getPath(resourceId));
		} catch (IOException e) {
			if (e instanceof FileNotFoundException)
				throw new FileNotFoundException("Could not find " + resourceId + " of resource type " + this.type + ".");
			throw e;
		}
	}

	public InputStream open(Identifier resourceId) throws IOException {
		try {
			return Files.newInputStream(this.getPath(resourceId));
		} catch (IOException e) {
			if (e instanceof FileNotFoundException)
				throw new FileNotFoundException("Could not find " + resourceId + " of resource type " + this.type + ".");
			throw e;
		}
	}

	public static ResourceManager getDefault(ResourceType type) {
		return RESOURCE_MANAGERS.computeIfAbsent(type, t -> {
			try {
				return new ResourceManager(type, getSelfRoot());
			} catch (URISyntaxException | IOException e) {
				throw new RuntimeException("Could not create default resource manager.", e);
			}
		});
	}

	private static Path getSelfRoot() throws URISyntaxException, IOException {
		var uri = Objects.requireNonNull(ResourceManager.class.getResource('/' + Constants.RESOURCES_ROOT_FILE_NAME)).toURI();

		if (uri.getScheme().equals("jar")) {
			return FileSystems.newFileSystem(
					Paths.get(ResourceManager.class.getProtectionDomain().getCodeSource().getLocation().toURI())
			).getRootDirectories().iterator().next();
		} else {
			return Paths.get(uri).getParent().toAbsolutePath().normalize();
		}
	}
}
