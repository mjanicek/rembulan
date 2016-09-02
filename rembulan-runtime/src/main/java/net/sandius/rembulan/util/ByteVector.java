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

public class ByteVector {

	private final byte[] bytes;

	private ByteVector(byte[] bytes) {
		this.bytes = Check.notNull(bytes);
	}

	public static ByteVector wrap(byte[] bytes) {
		return new ByteVector(bytes);
	}

	public static ByteVector copyFrom(byte[] bytes) {
		return wrap(Arrays.copyOf(bytes, bytes.length));
	}

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

	public int size() {
		return bytes.length;
	}

	public byte get(int index) {
		return bytes[index];
	}

	public byte[] copyToNewArray() {
		return Arrays.copyOf(bytes, bytes.length);
	}

}
