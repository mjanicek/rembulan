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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

/**
 * A byte string backed by a {@link java.lang.String}.
 */
class StringByteString extends ByteString {

	private final String string;
	private final Charset charset;

	private int byteHashCode;
	private int byteLength;

	StringByteString(String s, Charset charset) {
		this.string = Objects.requireNonNull(s);
		this.charset = Objects.requireNonNull(charset);
		this.byteHashCode = 0;
		this.byteLength = 0;
	}

	@Override
	protected boolean equals(ByteString that) {
		if (that instanceof StringByteString) {
			StringByteString sbs = (StringByteString) that;
			if (this.charset.equals(sbs.charset)) {
				return this.string.equals(sbs.string);
			}
			else {
				// Don't force byteHashCode computation, but use it if we know that it has already
				// been computed.
				int thisHash = this.byteHashCode;
				int thatHash = ((StringByteString) that).byteHashCode;
				if (thisHash != 0 && thatHash != 0 && thisHash != thatHash) return false;
			}
		}

		// TODO: do this incrementally

		if (this.length() != that.length()) return false;

		int len = this.length();
		for (int i = 0; i < len; i++) {
			if (this.byteAt(i) != that.byteAt(i)) return false;
		}

		return true;
	}

	private int computeHashCode() {
		// FIXME: do this incrementally
		int hc = 0;

		byte[] bytes = string.getBytes(charset);

		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i];
			hc = (hc * 31) + (b & 0xff);
		}

		return hc;
	}

	@Override
	public int hashCode() {
		int hc = byteHashCode;

		if (hc == 0 && string.length() > 0) {
			hc = computeHashCode();

			// update cached hashCode
			byteHashCode = hc;
		}

		return hc;
	}

	@Override
	public int length() {
		int len = byteLength;
		if (len == 0 && string.length() > 0) {
			// FIXME: get bytes from a cache
			byte[] bytes = string.getBytes();
			len = bytes.length;

			// update cached length
			byteLength = len;
		}
		return len;
	}

	@Override
	public boolean isEmpty() {
		return string.isEmpty();
	}

	@Override
	public byte byteAt(int index) {
		// FIXME: cache it
		return string.getBytes(charset)[index];
	}

	@Override
	public ByteString substring(int start, int end) {
		// FIXME: cache it
		byte[] bytes = string.getBytes(charset);

		if (start > end || start < 0 || end < 0 || end > bytes.length) {
			throw new IndexOutOfBoundsException();
		}

		return new ArrayByteString(Arrays.copyOfRange(bytes, start, end));
	}

	@Override
	public String toString() {
		return string;
	}

	@Override
	public String decode(Charset charset) {
		if (this.charset.equals(charset)) {
			return string;
		}
		else {
			return super.decode(charset);
		}
	}

	@Override
	public byte[] getBytes() {
		// FIXME: get from cache
		return string.getBytes(charset);
	}

	@Override
	public void putTo(ByteBuffer buffer) {
		buffer.put(getBytes());
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(getBytes());
	}

	@Override
	public int compareTo(ByteString other) {
		if (other instanceof StringByteString) {
			StringByteString that = (StringByteString) other;
			if (this.charset.equals(that.charset)) {
				return this.string.compareTo(that.string);
			}
		}

		return super.compareTo(other);
	}

	@Override
	public ByteString concat(ByteString other) {
		if (other instanceof StringByteString) {
			StringByteString that = (StringByteString) other;
			if (this.charset.equals(that.charset)) {
				return ByteString.of(this.string.concat(that.string));
			}
		}

		return super.concat(other);
	}

	@Override
	public boolean startsWith(byte b) {
		if (string.isEmpty()) return false;

		// FIXME: this assumes that the 1st char of the encoded string can be directly compared
		// to the argument. This is wrong for many encodings, incl. UTF-8, where this
		// is only true for b < 128. (I.e., ASCII).

		return string.charAt(0) == (char) b;
	}

}
