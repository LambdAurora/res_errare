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

package dev.lambdaurora.res_errare.util;

import java.util.Objects;

public class Identifier {
	private static final String ALLOWED_NAMESPACE_CHARACTERS;
	private static final String ALLOWED_PATH_CHARACTERS;

	private final String namespace;
	private final String path;

	public Identifier(String namespace, String path) {
		check(namespace, path);
		this.namespace = namespace;
		this.path = path;
	}

	public final String namespace() {
		return this.namespace;
	}

	public final String path() {
		return this.path;
	}

	public Identifier concat(String rest) {
		return new Identifier(this.namespace, this.path + rest);
	}

	public Identifier sub(String subPath) {
		var separator = "/";
		if (this.path.endsWith(separator))
			separator = "";

		return this.concat(separator + subPath);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var that = (Identifier) o;
		return Objects.equals(this.namespace, that.namespace) && Objects.equals(this.path, that.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.namespace, this.path);
	}

	@Override
	public String toString() {
		return this.namespace + ':' + this.path;
	}

	private static void check(String namespace, String path) {
		int namespaceIndex;
		for (namespaceIndex = 0; namespaceIndex < namespace.length(); namespaceIndex++) {
			char c = namespace.charAt(namespaceIndex);
			if (!ALLOWED_NAMESPACE_CHARACTERS.contains(String.valueOf(c))) {
				throw new InvalidIdentifierException("Invalid identifier namespace for (" + namespace + ":" + path
						+ ") at position " + namespaceIndex + ", invalid character '" + c + "'.");
			}
		}

		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			if (!ALLOWED_PATH_CHARACTERS.contains(String.valueOf(c))) {
				throw new InvalidIdentifierException("Invalid identifier path for (" + namespace + ":" + path
						+ ") at position " + (i + namespaceIndex + 1) + ", invalid character '" + c + "'.");
			}
		}
	}

	static {
		var builder = new StringBuilder();

		for (char c = 'a'; c <= 'z'; c++)
			builder.append(c);

		for (char c = '0'; c <= '9'; c++)
			builder.append(c);

		builder.append("._-");
		ALLOWED_NAMESPACE_CHARACTERS = builder.toString();

		builder.append('/');
		ALLOWED_PATH_CHARACTERS = builder.toString();
	}
}
