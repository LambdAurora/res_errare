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
