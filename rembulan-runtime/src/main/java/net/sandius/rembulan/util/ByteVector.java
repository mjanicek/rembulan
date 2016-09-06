/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * A read-only array of bytes.
 */
public final class ByteVector {

	private final byte[] bytes;

	private ByteVector(byte[] bytes) {
		this.bytes = Objects.requireNonNull(bytes);
	}

	/**
	 * Wraps the byte array {@code bytes} into a byte vector.
	 *
	 * <p>Note that the byte vector will be backed by {@code bytes}, so any changes
	 * of the contents of {@code bytes} will be reflected in the resulting byte vector.</p>
	 *
	 * @param bytes  the byte array, must not be {@code null}
	 * @return  a byte vector wrapping {@code bytes}
	 *
	 * @throws NullPointerException  if {@code bytes} is {@code null}
	 */
	public static ByteVector wrap(byte[] bytes) {
		return new ByteVector(bytes);
	}

	/**
	 * Creates a new byte vectory from a copy of {@code bytes}.
	 *
	 * @param bytes  the byte array, must not be {@code null}
	 * @return  a byte vector containing a copy of {@code bytes}
	 *
	 * @throws NullPointerException  if {@code bytes} is {@code null}
	 */
	public static ByteVector copyFrom(byte[] bytes) {
		return wrap(Arrays.copyOf(bytes, bytes.length));
	}

	/**
	 * Returns {@code true} iff {@code o} is a byte vector with contents identical
	 * to this byte vector.
	 *
	 * @param o  the object to test for equality with this byte vector
	 * @return  {@code true} iff {@code o} is a byte vector with identical contents
	 *          to this byte vector
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ByteVector that = (ByteVector) o;

		return Arrays.equals(bytes, that.bytes);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(bytes);
	}

	/**
	 * Returns the length of the byte vector (i.e., the length of the underlying array).
	 *
	 * @return  the length of this byte vector
	 */
	public int size() {
		return bytes.length;
	}

	/**
	 * Returns the {@code index}-th element of the byte vector.
	 *
	 * @param index  the index, must be in the range between 0 (inclusive) and {@code size()}
	 *               (exclusive)
	 * @return  the {@code index}-th element of this byte vector
	 *
	 * @throws ArrayIndexOutOfBoundsException  if {@code index} is outside of the permitted
	 * 	                                       range
	 */
	public byte get(int index) {
		return bytes[index];
	}

	/**
	 * Copy the contents of this byte vector to a freshly allocated new array.
	 *
	 * @return  a new array containing a copy of the contents of this byte vector
	 */
	public byte[] copyToNewArray() {
		return Arrays.copyOf(bytes, bytes.length);
	}

}
