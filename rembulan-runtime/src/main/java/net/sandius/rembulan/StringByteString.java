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

import net.sandius.rembulan.util.ByteIterator;
import net.sandius.rembulan.util.CharsetEncoderByteIterator;

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
		if (!charset.canEncode()) {
			throw new IllegalArgumentException("Charset cannot encode: " + charset.name());
		}
		this.byteHashCode = 0;
		this.byteLength = string.isEmpty() ? 0 : -1;
	}

	@Override
	protected boolean equals(ByteString that) {
		if (this.isEmpty() && that.isEmpty()) return true;

		// don't force hashCode computation, but use if already known
		int thisHash = this.maybeHashCode();
		int thatHash = that.maybeHashCode();
		if (thisHash != 0 && thatHash != 0 && thisHash != thatHash) return false;

		// don't force length computation, but use if already known
		int thisLength = this.maybeLength();
		int thatLength = that.maybeLength();
		if (thisLength >= 0 && thatLength >= 0 && thisLength != thatLength) return false;

		// compare byte-by-byte
		ByteIterator thisIterator = this.byteIterator();
		ByteIterator thatIterator = that.byteIterator();
		while (thisIterator.hasNext() && thatIterator.hasNext()) {
			byte thisByte = thisIterator.nextByte();
			byte thatByte = thatIterator.nextByte();
			if (thisByte != thatByte) {
				return false;
			}
		}

		return thisIterator.hasNext() == thatIterator.hasNext();
	}

	private int computeHashCode() {
		int hc = 0;

		ByteIterator it = new CharsetEncoderByteIterator(string, charset);
		while (it.hasNext()) {
			hc = (hc * 31) + (it.nextByte() & 0xff);
		}

		return hc;
	}

	@Override
	public int hashCode() {
		int hc = byteHashCode;

		if (hc == 0 && !string.isEmpty()) {
			hc = computeHashCode();

			// update cached hashCode
			byteHashCode = hc;
		}

		return hc;
	}

	@Override
	int maybeHashCode() {
		return byteHashCode;
	}

	private int computeLength() {
		int len = 0;
		ByteIterator it = new CharsetEncoderByteIterator(string, charset);
		while (it.hasNext()) {
			it.nextByte();
			len++;
		}
		return len;
	}

	@Override
	public int length() {
		int len = byteLength;
		if (len < 0) {
			len = computeLength();
			byteLength = len;
		}
		return len;
	}

	@Override
	int maybeLength() {
		return byteLength;
	}

	@Override
	public boolean isEmpty() {
		return string.isEmpty();
	}

	// must not escape, may be an array from the cache!
	private byte[] toBytes() {
		// TODO: cache the result
		return string.getBytes(charset);
	}

	@Override
	public byte[] getBytes() {
		byte[] bytes = toBytes();

		// must make a defensive copy
		return Arrays.copyOf(bytes, bytes.length);
	}

	@Override
	public byte byteAt(int index) {
		if (index < 0) {
			// don't even have to convert to bytes
			throw new IndexOutOfBoundsException(String.valueOf(index));
		}

		return toBytes()[index];
	}

	@Override
	public ByteIterator byteIterator() {
		return new CharsetEncoderByteIterator(string, charset);
	}

	@Override
	public ByteString substring(int start, int end) {
		byte[] bytes = toBytes();

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
	public void putTo(ByteBuffer buffer) {
		// ByteBuffer cannot be directly extended: it's safe to use a possibly cached array
		buffer.put(toBytes());
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		// OutputStream can be extended: pass a defensive copy
		stream.write(getBytes());
	}

	@Override
	public ByteString concat(ByteString other) {
		if (other instanceof StringByteString) {
			StringByteString that = (StringByteString) other;
			if (this.charset.equals(that.charset)) {
				// Caveat: preserves malformed characters and characters unmappable by charset
				return ByteString.of(this.string.concat(that.string));
			}
		}

		return super.concat(other);
	}

	@Override
	public boolean startsWith(byte b) {
		if (string.isEmpty()) return false;
		ByteIterator it = new CharsetEncoderByteIterator(string, charset);
		return it.hasNext() && it.nextByte() == b;
	}

}
