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
 * characters corresponding to 16-bit code units in the Basic Multilingual Plane (BMP).)
 * and Lua strings (raw 8-bit sequences).</p>
 *
 * <p>Byte strings may be created in three basic ways:</p>
 * <ul>
 *     <li>by constructing a <b>lossy</b> 8-bit view of a Java string using
 *       {@link #viewOf(String)};</li>
 *     <li>by copying from a byte array using {@link #copyOf(byte[])};</li>
 *     <li>by encoding a {@code java.lang.String} into bytes using {@link #encode(String)}
 *       and {@link #encode(String, Charset)}.</li>
 * </ul>
 *
 * <p>{@code ByteString} implements {@link CharSequence} in order to enable a degree
 * of interoperability with other Java-based character processing classes. A byte string
 * is a character sequence in the following manner:</p>
 * <ul>
 *     <li>every byte of the byte string is mapped directly to a character (in the
 *       range U+0000 to U+00FF) by the method {@link #charAt(int)};</li>
 *     <li>the method {@link #length()} returns the number of bytes in the byte string.</li>
 * </ul>
 *
 * <p>In other words, the way a byte string is to be seen as a character array is analogous
 * to <i>casting</i> the {@code byte} values it contains to {@code char}s. This is also
 * the interpretation used by the method {@link #toString()}. Note especially that this
 * does not take charsets into account. {@code ByteString} therefore provides the method
 * {@link #toString(Charset)} that uses a {@link Charset} to decode the byte string.</p>
 *
 * <p>The {@link #hashCode()} of a byte string is defined using the same function as
 * that of {@link String#hashCode()}, using the above definition of {@code charAt()}. This
 * means that for a raw 8-bit string {@code s}, the following will be true:</p>
 * <pre>
 *     s.hashCode() == ByteString.viewOf(s).hashCode()
 * </pre>
 */
public abstract class ByteString implements CharSequence, Comparable<ByteString> {

	ByteString() {
		// no-op: package-private to restrict access
	}

	/**
	 * Returns an 8-bit view of the string {@code s}.
	 *
	 * <p>The byte string will be backed by {@code s}, i.e., no copying of its contents
	 * will be performed by this method.</p>
	 *
	 * <p>Characters in the resulting byte string are mapped to bytes by dropping the more
	 * significant byte. Therefore, the byte string obtained this way is not suitable for
	 * use with strings that contain characters outside of the range U+0000 to U+00FF (inclusive).
	 * (Note that this code point range corresponds to Basic Latin, Latin-1 Supplement and
	 * C0 and C1 Control characters.)</p>
	 *
	 * <p>In other words, <b>when {@code s} contains a character outside of U+0000 and U+00FF,
	 * its 8-bit view will not correspond to the same string</b>, and the missing information
	 * will be effectively lost from its byte representation. To encode a {@code java.lang.String}
	 * into a {@code ByteString} without such of information, use {@link #encode(String)}
	 * or {@link #encode(String, Charset)}.</p>
	 *
	 * @param s  the string to take the byte view of, must not be {@code null}
	 * @return  a byte view of {@code s}
	 *
	 * @throws NullPointerException  if {@code s} is {@code null}
	 */
	public static ByteString viewOf(String s) {
		return new StringViewByteString(s);
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

	public static ByteString recode(String s) {
		ArrayByteString bs = new ArrayByteString(s.getBytes());
		return new StringViewByteString(bs.toString());
	}

	public static ByteString recode(String s, Charset charset) {
		ArrayByteString bs = new ArrayByteString(s.getBytes(charset));
		return new StringViewByteString(bs.toString());
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
	 * Returns the byte at position {@code index} mapped to a character.
	 *
	 * <p>Java characters represent UTF-16 code units (including surrogates). This method
	 * therefore returns a character corresponding to a Unicode code point between U+0000
	 * and U+00FF within the Basic Multilingual Plane (BMP).</p>
	 *
	 * @param index  the position in the string
	 * @return  the byte at position {@code index} converted to a character
	 *
	 * @throws IndexOutOfBoundsException  if {@code index < 0} or {@code index >= length()}
	 */
	@Override
	public char charAt(int index) {
		return (char) (byteAt(index) & 0xff);
	}

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

	/**
	 * Returns a string representation of this byte string where each byte is directly
	 * mapped to a Java character.
	 *
	 * <p>Java characters represent UTF-16 code units (including surrogates). This method
	 * therefore returns a string where bytes are converted to Unicode code points U+0000
	 * to U+00FF within the Basic Multilingual Plane (BMP).</p>
	 *
	 * <p>This method does <b>not</b> take any character encoding into account;
	 * most importantly, it does not use the platform's charset. In order to decode the byte
	 * string using a charset, use {@link #toString(Charset)}.</p>
	 *
	 * @return  a string obtained from this byte string by mapping each byte to a character
	 */
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
	public String toString(Charset charset) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(length());
		putTo(byteBuffer);
		byteBuffer.rewind();
		return charset.decode(byteBuffer).toString();
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
