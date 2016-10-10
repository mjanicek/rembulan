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
 * Byte string, an immutable sequence of bytes.
 *
 * <p>The purpose of this class is to serve as a bridge between Java strings (with their
 * characters corresponding to 16-bit code units in the Basic Multilingual Plane (BMP))
 * and Lua strings (raw 8-bit sequences).</p>
 *
 * <p>The {@link #hashCode()} of a byte string is defined using the same function as
 * that of {@link String#hashCode()} with bytes treated as unsigned integers. This means
 * that for a raw 8-bit string {@code s}, the following will be true:</p>
 * <pre>
 *     s.hashCode() == ByteString.viewOf(s).hashCode()
 * </pre>
 */
public abstract class ByteString implements Comparable<ByteString> {

	ByteString() {
		// no-op: package-private to restrict access
	}

	public static ByteString of(String s, Charset charset) {
		return new StringByteString(s, charset);
	}

	public static ByteString of(String s) {
		return of(s, Charset.defaultCharset());
	}

	static ByteString wrap(byte[] bytes) {
		return new ArrayByteString(bytes);
	}

	/**
	 * Returns a byte string containing a copy of the bytes {@code bytes}.
	 *
	 * @param bytes  the byte array to use as the byte string, must not be {@code null}
	 * @return  a byte string containing a copy of {@code bytes}
	 */
	public static ByteString copyOf(byte[] bytes) {
		return wrap(Arrays.copyOf(bytes, bytes.length));
	}

	/**
	 * Returns the string {@code s} converted to a byte string using the platform's default
	 * charset.
	 *
	 * @param s  the string to convert to bytes, must not be {@code null}
	 * @return  the string {@code s} encoded into bytes using the platform's default charset
	 */
	public static ByteString encode(String s) {
		return wrap(s.getBytes());
	}

	/**
	 * Returns the string {@code s} converted to a byte string using the specified charset
	 * {@code charset}.
	 *
	 * @param s  the string to convert to bytes, must not be {@code null}
	 * @param charset  the charset to use to decode {@code s} into bytes, must not be {@code null}
	 * @return  the string {@code s} encoded into bytes using {@code charset}
	 *
	 * @throws NullPointerException  if {@code s} or {@code charset} is {@code null}
	 */
	public static ByteString encode(String s, Charset charset) {
		return wrap(s.getBytes(charset));
	}

	/**
	 * Returns {@code true} if {@code o} is a byte string with contents equal
	 * to this byte string.
	 *
	 * @param o  object to evaluate equality with, may be {@code null}
	 * @return  {@code true} iff {@code o} is a byte string with equal contents to {@code this}
	 */
	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof ByteString && this.equals((ByteString) o);
	}

	/**
	 * Returns the hash code of this byte string. The hash code is computed using the same
	 * function as used by {@link String#hashCode()}.
	 *
	 * @return  the hash code of this byte string
	 */
	@Override
	public abstract int hashCode();

	protected abstract boolean equals(ByteString that);

	/**
	 * Returns a new byte array containing the bytes of this byte string.
	 *
	 * @return  a new byte array
	 */
	public abstract byte[] getBytes();

	/**
	 * Returns the byte at position {@code index}.
	 *
	 * @param index  the position in the string
	 * @return  the byte at position {@code index}
	 *
	 * @throws IndexOutOfBoundsException  if {@code index < 0} or {@code index >= length()}
	 */
	public abstract byte byteAt(int index);

	/**
	 * Returns the length of this byte string, i.e., the number of bytes it contains.
	 *
	 * @return  the length of this byte string
	 */
	public abstract int length();

	// TODO: doc
	public abstract ByteString substring(int start, int end);

	/**
	 * Puts the contents of this byte string to the specified {@code buffer}.
	 *
	 * @param buffer  the buffer to use, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code buffer} is {@code null}
	 */
	public abstract void putTo(ByteBuffer buffer);

	/**
	 * Writes the contents of this byte string to the specified {@code stream}.
	 *
	 * @param stream  the stream to use, must not be {@code null}
	 *
	 * @throws IOException  when I/O error happens during the write
	 * @throws NullPointerException  if {@code stream} is {@code null}
	 */
	public abstract void writeTo(OutputStream stream) throws IOException;

	@Override
	public abstract String toString();

	/**
	 * Returns a string representation of this byte string that uses the specified
	 * charset {@code charset} to decode characters from bytes.
	 *
	 * @param charset  the charset to use, must not be {@code null}
	 * @return  this byte string decoded into a string using {@code charset}
	 *
	 * @throws NullPointerException  if {@code charset} is {@code null}
	 */
	public String decode(Charset charset) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(length());
		putTo(byteBuffer);
		byteBuffer.rewind();
		return charset.decode(byteBuffer).toString();
	}

	/**
	 * Returns a string represented by this byte string decoded using the default charset
	 * of the virtual machine.
	 *
	 * <p>This is effectively equivalent to {@link #decode(Charset)}
	 * called with {@link Charset#defaultCharset()}.</p>
	 *
	 * @return  a string decoded from this byte string using the platform's default
	 *          charset
	 */
	public String decode() {
		return decode(Charset.defaultCharset());
	}

	/**
	 * Compares this byte string lexicographically with {@code that}. Returns a negative
	 * integer, zero, or a positive integer if {@code this} is lesser than, equal to or greater
	 * than {@code that} in this ordering.
	 *
	 * <p>For the purposes of this ordering, bytes are interpreted as <i>unsigned</i>
	 * integers.</p>
	 *
	 * @param that  byte string to compare to
	 * @return  a negative, zero, or positive integer if {@code this} is lexicographically
	 *          lesser than, equal to or greater than {@code that}
	 */
	@Override
	public int compareTo(ByteString that) {
		Objects.requireNonNull(that);

		int len = Math.min(this.length(), that.length());
		for (int i = 0; i < len; i++) {
			int a = this.byteAt(i) & 0xff;
			int b = that.byteAt(i) & 0xff;
			int cmp = a - b;
			if (cmp != 0) return cmp;
		}

		return this.length() - that.length();
	}

}
