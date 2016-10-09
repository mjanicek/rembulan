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

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * An immutable view of a {@link java.lang.String} that treats all characters of
 * the underlying strings as 8-bit values by taking the less significant byte.
 */
public final class StringViewByteString extends ByteString {

	private final String string;

	private int hashCode;

	/*
	 * Updated in hashCode() and computeIsRaw().
	 *
	 * May be one of three values:
	 *   0 -- uninitialised;
	 *   1 -- all chars are 8-bit;
	 *   2 -- at least one character is wider than 8 bits.
	 */
	private int originalIsRaw;

	private static final int ALL_8_BIT = 1;
	private static final int TRUNCATED = 2;

	StringViewByteString(String s) {
		this.string = Objects.requireNonNull(s);
		this.hashCode = 0;
		this.originalIsRaw = 0;
	}

	static StringViewByteString decoded(String s) {
		ArrayByteString bs = new ArrayByteString(s.getBytes());
		return new StringViewByteString(bs.toString());
	}

	static StringViewByteString decoded(String s, Charset charset) {
		ArrayByteString bs = new ArrayByteString(s.getBytes(charset));
		return new StringViewByteString(bs.toString());
	}

	@Override
	protected boolean equals(ByteString that) {
		if (this.length() != that.length()) return false;

		if (that instanceof StringViewByteString) {
			// Don't force hashCode computation, but use it if we know that it has already
			// been computed.
			int thisHash = this.hashCode;
			int thatHash = ((StringViewByteString) that).hashCode;
			if (thisHash != 0 && thatHash != 0 && thisHash != thatHash) return false;
		}

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
			if (string.length() > 0) {

				/*
				 * Assume that the hashCode has not been computed yet, so compute it and update
				 * the hashCode field.
				 *
				 * This is okay to do without synchronisation, since the underlying string
				 * is immutable -- we will do this computation twice if two threads get to this
				 * point at the same time, but the result they get will be the same.
				 *
				 * Caveat: hashCode may actually be 0, in which case it will be re-computed
				 * every time hashCode() is called.
				 *
				 * The hash function is the same as the one documented by String.hashCode(),
				 * but we only consider the least significant byte of each character.
				 */

				int mask = 0;

				for (int i = 0; i < string.length(); i++) {
					int ch = string.charAt(i);
					hc = (hc * 31) + (ch & 0xff);
					mask |= ch;
				}

				// update the originalIsRaw field
				originalIsRaw = (mask & 0xff) == mask ? ALL_8_BIT : TRUNCATED;

				// update the hashCode field
				hashCode = hc;
			}
			else {
				// string is empty, i.e., its hashCode is 0
			}
		}

		return hc;
	}

	@Override
	public int length() {
		return string.length();
	}

	@Override
	public byte byteAt(int index) {
		return (byte) string.charAt(index);
	}

	@Override
	public char charAt(int index) {
		return (char) (byteAt(index) & 0xff);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return new StringViewByteString(string.substring(start, end));
	}

	private String toConvertedString() {
		char[] chars = new char[string.length()];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = charAt(i);
		}
		return String.valueOf(chars);
	}

	/**
	 * Returns this byte view converted to a string.
	 *
	 * @return
	 */
	@Override
	public String toString() {
		if (isOriginalRaw8Bit()) {
			return string;
		}
		else {
			return toConvertedString();
		}
	}

	/**
	 * Returns the underlying string.
	 *
	 * @return  the underlying string
	 */
	public String getOriginalString() {
		return string;
	}

	@Override
	public byte[] getBytes() {
		byte[] bytes = new byte[string.length()];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) charAt(i);
		}
		return bytes;
	}

	private boolean computeIsRaw() {
		int len = string.length();
		for (int i = 0; i < len; i++) {
			int ch = string.charAt(i);
			if ((ch & 0xff) != ch) return false;
		}
		return true;
	}

	/**
	 * Returns {@code true} iff all characters in the underlying string fit into 8 bits.
	 *
	 * In other words, returns {@code false} if the underlying string contains at least
	 * one character wider than 8-bits.
	 *
	 * @return  {@code true} if the underlying string is raw 8-bit
	 */
	public boolean isOriginalRaw8Bit() {
		int o = originalIsRaw;
		if (o == 0) {
			boolean result = computeIsRaw();
			originalIsRaw = result ? ALL_8_BIT : TRUNCATED;
			return result;
		}
		else {
			return o == ALL_8_BIT;
		}
	}

}
