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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

class ArrayByteString extends ByteString {

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
				for (int i = 0; i < bytes.length; i++) {
					int b = bytes[i];
					hc = (hc * 31) + (b & 0xff);
				}
				hashCode = hc;
			}
		}

		return hc;
	}

	@Override
	public String toString() {
		char[] chars = new char[bytes.length];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) (bytes[i] & 0xff);
		}
		return String.valueOf(chars);
	}

	@Override
	public int length() {
		return bytes.length;
	}

	@Override
	public byte byteAt(int index) {
		return bytes[index];
	}

	@Override
	public char charAt(int index) {
		return (char) (bytes[index] & 0xff);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
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
		stream.write(bytes);
	}

}
