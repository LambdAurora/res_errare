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

import jdk.incubator.foreign.CLinker;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public enum NativeSizes {
	;

	/* Primitives */
	public static final long BYTE_SIZE = CLinker.C_CHAR.byteSize();
	public static final long INT_SIZE = CLinker.C_INT.byteSize();
	public static final long LONG_SIZE = CLinker.C_LONG.byteSize();
	public static final long FLOAT_SIZE = CLinker.C_FLOAT.byteSize();
	public static final long DOUBLE_SIZE = CLinker.C_DOUBLE.byteSize();

	public static final long VEC4_LENGTH = 4;
	public static final long VEC4F_SIZE = FLOAT_SIZE * VEC4_LENGTH;

	public static final long MATRIX4_LENGTH = VEC4_LENGTH * VEC4_LENGTH;
	public static final long MATRIX4F_SIZE = FLOAT_SIZE * MATRIX4_LENGTH;

	public static long sizeof(float value) {
		return sizeof(float.class);
	}

	public static long sizeof(byte[] value) {
		return value.length * BYTE_SIZE;
	}

	public static long sizeof(float[] value) {
		return value.length * FLOAT_SIZE;
	}

	public static long sizeof(Class<?> clazz) {
		if (clazz == byte.class)
			return BYTE_SIZE;
		else if (clazz == int.class)
			return INT_SIZE;
		else if (clazz == long.class)
			return LONG_SIZE;
		else if (clazz == float.class)
			return FLOAT_SIZE;
		else if (clazz == double.class)
			return DOUBLE_SIZE;
		else if (Vector4f.class.isAssignableFrom(clazz))
			return VEC4F_SIZE;
		else if (Matrix4f.class.isAssignableFrom(clazz))
			return MATRIX4F_SIZE;

		throw new IllegalArgumentException("Cannot determine size of type " + clazz + ".");
	}

	public static long sizeof(Object value) {
		return sizeof(value.getClass());
	}

	public static long lengthOf(Object value) {
		if (value instanceof Matrix4f)
			return MATRIX4_LENGTH;
		return -1;
	}
}
