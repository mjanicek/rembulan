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

import java.io.IOException;
import java.io.InputStream;
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
 * <p>Byte strings come in two flavours:</p>
 * <ul>
 *     <li>one is a wrapper of {@link String java.lang.String} with a given {@link Charset}
 *       &mdash; constructed using {@link #of(String)};</li>
 *     <li>the other is a wrapper of a {@code byte} arrays
 *       &mdash; constructed using {@link #wrap(byte[])}.</li>
 * </ul>
 *
 * <p>The {@code ByteString} class provides the functionality for treating both cases
 * as sequences of bytes when they take part in Lua operations, and as Java strings when
 * used by an outer Java application. However, these perspectives are as <i>lazy</i>
 * as possible, in order to avoid doing unnecessary work.</p>
 *
 * <p>This class provides a natural lexicographical ordering that is consistent with equals.</p>
 */
public abstract class ByteString implements Comparable<ByteString> {

	ByteString() {
		// no-op: package-private to restrict access
	}

	/**
	 * Returns a new byte string corresponding to the bytes in {@code s} as encoded
	 * by the specified {@code charset}.
	 *
	 * @param s  the string to take the byte view of, must not be {@code null}
	 * @param charset  the charset to use for decoding {@code s} into bytes, must not be {@code null}
	 * @return  a byte string perspective of {@code s} using {@code charset}
	 *
	 * @throws NullPointerException  if {@code s} or {@code charset} is {@code null}
	 * @throws IllegalArgumentException  if {@code charset} does not provide encoding
	 *                                   capability (see {@link Charset#canEncode()})
	 */
	public static ByteString of(String s, Charset charset) {
		return new StringByteString(s, charset);
	}

	/**
	 * Returns a new byte string corresponding to the bytes in {@code s} as encoded
	 * by the default charset ({@link Charset#defaultCharset()}).
	 *
	 * @param s  the string to take the perspective of, must not be {@code null}
	 * @return  a byte string perspective of {@code s}
	 *
	 * @throws NullPointerException  if {@code s} is {@code null}
	 */
	public static ByteString of(String s) {
		return of(s, Charset.defaultCharset());
	}

	/**
	 * Returns a new byte string corresponding to bytes in {@code s} by taking the
	 * least significant byte of each character.
	 *
	 * @param s  the string to get bytes from, must not be {@code null}
	 * @return  a byte string based on {@code s} by taking the least significant
	 *          byte of each char
	 *
	 * @throws NullPointerException  if {@code s} is {@code null}
	 */
	public static ByteString fromRaw(String s) {
		byte[] bytes = new byte[s.length()];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (s.charAt(i) & 0xff);
		}
		return wrap(bytes);
	}

	/**
	 * Returns a byte string corresponding to the bytes in {@code s} as encoded by the default
	 * charset in a form suitable for use as a string constant.
	 *
	 * <p>This method differs from {@link #of(String)} in that it may force the computation
	 * of eagerly-evaluated properties of the resulting string at instantiation time.</p>
	 *
	 * @param s  the string to get bytes from, must not be {@code null}
	 * @return  a byte string based on a byte perspective of {@code s}
	 */
	public static ByteString constOf(String s) {
		return of(s);
	}

	static ByteString wrap(byte[] bytes) {
		return new ArrayByteString(bytes);
	}

	/**
	 * Returns a byte string containing a copy of the byte array {@code bytes}.
	 *
	 * @param bytes  the byte array to use as the byte string, must not be {@code null}
	 * @return  a byte string containing a copy of {@code bytes}
	 *
	 * @throws NullPointerException  if {@code bytes} is {@code null}
	 */
	public static ByteString copyOf(byte[] bytes) {
		return copyOf(bytes, 0, bytes.length);
	}

	/**
	 * Returns a byte string containing a copy of a slice of the byte array {@code bytes}
	 * starting at the offset {@code offset} and consisting of {@code length} bytes.
	 *
	 * @param bytes  the byte array to use as the byte string, must not be {@code null}
	 * @param offset  offset in {@code bytes} to start reading from
	 * @param length  the number of bytes to copy from {@code bytes}
	 * @return  a byte string containing a copy of {@code bytes}
	 *
	 * @throws NullPointerException  if {@code bytes} is {@code null}
	 * @throws IndexOutOfBoundsException  if {@code offset} or {@code length} are negative,
	 *                                    or if {@code (offset + length)} is greater than
	 *                                    {@code bytes.length}
	 */
	public static ByteString copyOf(byte[] bytes, int offset, int length) {
		if (offset < 0 || length < 0 || (offset + length) > bytes.length) {
			throw new IndexOutOfBoundsException("offset=" + offset + ", length=" + length);
		}

		return wrap(Arrays.copyOfRange(bytes, offset, length));
	}

	/**
	 * Returns an empty byte string.
	 *
	 * @return  an empty byte string
	 */
	public static ByteString empty() {
		return ArrayByteString.EMPTY_INSTANCE;
	}

	/**
	 * Returns {@code true} if {@code o} is a byte string containing the same bytes as
	 * this byte string.
	 *
	 * <p><b>Note:</b> this method uses the strict interpretation of byte strings as byte
	 * sequences. It is therefore <b>not</b> necessarily true that for two byte strings {@code a}
	 * and {@code b}, the result of their comparison is the same as the result of comparing
	 * their images provided by {@link #toString()}:</p>
	 * <pre>
	 *     boolean byteEq = a.equals(b);
	 *     boolean stringEq = a.toString().equals(b.toString());
	 *
	 *     // may fail!
	 *     assert (byteEq == stringEq);
	 * </pre>
	 *
	 * @param o  object to evaluate for equality with, may be {@code null}
	 * @return  {@code true} iff {@code o} is a byte string with equal contents to {@code this}
	 */
	@Override
	public final boolean equals(Object o) {
		return this == o || o instanceof ByteString && this.equals((ByteString) o);
	}

	/**
	 * Returns the hash code of this byte string. The hash code is computed using the same
	 * function as used by {@link String#hashCode()}, interpreting the byte string's bytes
	 * as unsigned integers.
	 *
	 * @return  the hash code of this byte string
	 */
	@Override
	public abstract int hashCode();

	/**
	 * Returns an integer <i>i</i> that corresponds to the hash code of this byte string
	 * if <i>i</i> is non-zero. When <i>i</i> is zero, it <b>may or may not</b> be the hash code
	 * of this string.
	 *
	 * @return  the hash code of this byte string if non-zero
	 */
	abstract int maybeHashCode();

	abstract boolean equals(ByteString that);

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
	 * Returns an iterator over the bytes in this byte string.
	 *
	 * @return  an iterator over the bytes in this byte string
	 */
	public abstract ByteIterator byteIterator();

	/**
	 * Returns an input stream that reads the contents of this string.
	 *
	 * @return an input stream that reads the contents of this string
	 */
	public InputStream asInputStream() {
		return new ByteStringInputStream(byteIterator());
	}

	/**
	 * Returns the length of this byte string, i.e., the number of bytes it contains.
	 *
	 * @return  the length of this byte string
	 */
	public abstract int length();

	/**
	 * Returns an integer <i>i</i> that is equal to the length of this byte string if
	 * <i>i</i> is non-negative. When <i>i</i> is negative, the length of this byte string
	 * is not yet known.
	 *
	 * @return  the length of this byte string if non-negative
	 */
	abstract int maybeLength();

	/**
	 * Returns {@code true} iff this byte string is empty, i.e., if the number of bytes it
	 * contains is 0.
	 *
	 * @return  {@code true} iff this byte string is empty
	 */
	public abstract boolean isEmpty();

	/**
	 * Returns a substring of this byte string starting at position {@code start} (inclusive),
	 * ending at position {@code end} (exclusive).
	 *
	 * <p>The indices refer to the <i>byte</i> position in the byte string.</p>
	 *
	 * @param start  the first index to include in the new substring (inclusive)
	 * @param end  the smallest index immediately following the new substring in this byte string
	 * @return  a substring of this byte string ranging from {@code start} (inclusive)
	 *          to {@code end} (exclusive)
	 *
	 * @throws IndexOutOfBoundsException  if {@code start < 0}, {@code end > length()}
	 *                                    or {@code start > end}
	 */
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

	/**
	 * Returns the interpretation of this byte string as a Java string.
	 *
	 * @return  the string represented by this byte string
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
	public String decode(Charset charset) {
		if (isEmpty()) return "";

		ByteBuffer byteBuffer = ByteBuffer.allocate(length());
		putTo(byteBuffer);
		byteBuffer.flip();
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
	 * Returns a string in which all characters are directly mapped to the bytes in this
	 * byte string by treating them as unsigned integers.
	 *
	 * <p>This method is the complement of {@link #fromRaw(String)}.</p>
	 *
	 * @return  a raw string based on this byte string
	 */
	public abstract String toRawString();

	/**
	 * Compares this byte string lexicographically with {@code that}. Returns a negative
	 * integer, zero, or a positive integer if {@code this} is lesser than, equal to or greater
	 * than {@code that} in this ordering.
	 *
	 * <p>For the purposes of this ordering, bytes are interpreted as <i>unsigned</i>
	 * integers.</p>
	 *
	 * <p><b>Note:</b> this method uses the strict interpretation of byte strings as byte
	 * sequences. It is therefore <b>not</b> necessarily true that for two byte strings {@code a}
	 * and {@code b}, the result of their comparison is the same as the result of comparing
	 * their images provided by {@link #toString()}:</p>
	 * <pre>
	 *     int byteCmp = a.compareTo(b);
	 *     int stringCmp = a.toString().compareTo(b.toString());
	 *
	 *     // may fail!
	 *     assert(Integer.signum(byteCmp) == Integer.signum(stringCmp));
	 * </pre>
	 *
	 * <p>This is done in order to ensure that the natural ordering provided by this
	 * {@code compareTo()} is consistent with equals.</p>
	 *
	 * @param that  byte string to compare to, must not be {@code null}
	 * @return  a negative, zero, or positive integer if {@code this} is lexicographically
	 *          lesser than, equal to or greater than {@code that}
	 *
	 * @throws NullPointerException  if {@code that} is {@code null}
	 */
	@Override
	public int compareTo(ByteString that) {
		Objects.requireNonNull(that);

		ByteIterator thisIterator = this.byteIterator();
		ByteIterator thatIterator = that.byteIterator();

		while (thisIterator.hasNext() && thatIterator.hasNext()) {
			int thisByte = thisIterator.nextByte() & 0xff;
			int thatByte = thatIterator.nextByte() & 0xff;
			int diff = thisByte - thatByte;
			if (diff != 0) return diff;
		}

		return thisIterator.hasNext()
				? 1  // !thatIterator.hasNext() => that is shorter
				: thatIterator.hasNext()
						? -1  // this is shorter
						: 0;  // equal length
	}

	/**
	 * Returns a byte string formed by a concatenating this byte string with the byte string
	 * {@code other}.
	 *
	 * <p><b>Note:</b> this method uses the non-strict interpretation and therefore
	 * may (<i>but might not necessarily</i>) preserve unmappable and malformed characters
	 * occurring in the two strings.</p>
	 *
	 * @param other  the byte string to concatenate this byte string with, must not be {@code null}
	 * @return  this byte string concatenated with {@code other}
	 *
	 * @throws NullPointerException  if {@code other} is {@code null}
	 */
	public ByteString concat(ByteString other) {
		if (other.isEmpty()) return this;
		else if (this.isEmpty()) return other;

		byte[] thisBytes = this.getBytes();
		byte[] otherBytes = other.getBytes();

		byte[] result = new byte[thisBytes.length + otherBytes.length];
		System.arraycopy(thisBytes, 0, result, 0, thisBytes.length);
		System.arraycopy(otherBytes, 0, result, thisBytes.length, otherBytes.length);
		return ByteString.wrap(result);
	}

	/**
	 * Returns a byte string formed by concatenating this byte string with the string
	 * {@code other}.
	 *
	 * <p>This is a convenience method equivalent to</p>
	 * <pre>
	 *     concat(ByteString.of(other))
	 * </pre>
	 *
	 * @param other  the string to concatenate with, must not be {@code null}
	 * @return  this byte string concatenated with {@code other}
	 *
	 * @throws  NullPointerException  if {@code other} is {@code null}
	 */
	public ByteString concat(String other) {
		return this.concat(ByteString.of(other));
	}

	// TODO: add startsWith(ByteString)

	/**
	 * Returns {@code true} if the first byte of this byte string is {@code b}.
	 *
	 * @param b  the byte to compare the first byte of this byte string to
	 * @return  {@code true} if this byte string starts with {@code b}
	 */
	public abstract boolean startsWith(byte b);

	// TODO: add contains(ByteString)

	/**
	 * Returns {@code true} if the byte string contains the byte {@code b}.
	 *
	 * @param b  the byte to search for in the byte string
	 * @return  {@code true} if this byte string contains {@code b}
	 */
	public boolean contains(byte b) {
		ByteIterator it = byteIterator();
		while (it.hasNext()) {
			if (b == it.nextByte()) return true;
		}
		return false;
	}

}
