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

package net.sandius.rembulan;

import net.sandius.rembulan.util.ArrayByteIterator;
import net.sandius.rembulan.util.ByteIterator;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * A byte string backed by a byte array.
 */
class ArrayByteString extends ByteString {

	static final ArrayByteString EMPTY_INSTANCE = new ArrayByteString(new byte[0]);

	private final byte[] bytes;
	private int hashCode;

	ArrayByteString(byte[] bytes) {
		this.bytes = Objects.requireNonNull(bytes);
	}

	@Override
	protected boolean equals(ByteString that) {
		if (this.length() != that.length()) return false;

		int len = this.length();
		for (int i = 0; i < len; i++) {
			if (this.byteAt(i) != that.byteAt(i)) return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hc = hashCode;
		if (hc == 0) {
			if (bytes.length > 0) {
				for (byte b : bytes) {
					hc = (hc * 31) + (b & 0xff);
				}
				hashCode = hc;
			}
		}

		return hc;
	}

	@Override
	int maybeHashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return decode();
	}

	@Override
	public int length() {
		return bytes.length;
	}

	@Override
	int maybeLength() {
		return bytes.length;
	}

	@Override
	public boolean isEmpty() {
		return bytes.length == 0;
	}

	@Override
	public byte byteAt(int index) {
		return bytes[index];
	}

	@Override
	ByteIterator byteIterator() {
		return new ArrayByteIterator(bytes);
	}

	@Override
	public ByteString substring(int start, int end) {
		if (start > end || start < 0 || end < 0 || end > bytes.length) {
			throw new IndexOutOfBoundsException();
		}
		return new ArrayByteString(Arrays.copyOfRange(bytes, start, end));
	}

	@Override
	public byte[] getBytes() {
		return Arrays.copyOf(bytes, bytes.length);
	}

	@Override
	public void putTo(ByteBuffer buffer) {
		buffer.put(bytes);
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		// must make a defensive copy to avoid leaking the contents
		stream.write(getBytes());
	}

	@Override
	public boolean startsWith(byte b) {
		return bytes.length > 0 && bytes[0] == b;
	}

}
