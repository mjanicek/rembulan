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

import net.sandius.rembulan.util.Check;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A builder for byte strings, similar in interface to {@link StringBuilder}.
 *
 * <p>This class is not thread-safe.</p>
 */
public class ByteStringBuilder {

	private byte[] buffer;
	private int length;

	private static final int DEFAULT_CAPACITY = 32;

	// don't go any smaller than this
	private static final int MIN_CAPACITY = DEFAULT_CAPACITY;

	// returns the smallest positive integer i >= x such that i is a power of 2
	private static int binaryCeil(int x) {
		if (x < 0) return 0;
		// from "Hacker's Delight" by Henry S. Warren, Jr., section 3-2
		x -= 1;
		x |= (x >> 1);
		x |= (x >> 2);
		x |= (x >> 4);
		x |= (x >> 8);
		x |= (x >> 16);
		return x + 1;
	}

	private static int idealCapacity(int desired) {
		int ceil = binaryCeil(desired);
		return Math.max(ceil, MIN_CAPACITY);
	}

	private static byte[] resize(byte[] buf, int newSize) {
		assert (newSize >= buf.length);
		byte[] newBuf = new byte[newSize];
		System.arraycopy(buf, 0, newBuf, 0, buf.length);
		return newBuf;
	}

	private void ensureCapacity(int cap) {
		if (cap > buffer.length) {
			buffer = resize(buffer, idealCapacity(cap));
		}
	}

	private ByteStringBuilder(byte[] buffer, int length) {
		this.buffer = buffer;
		this.length = length;
	}

	/**
	 * Constructs a new empty {@code ByteStringBuilder} that can hold at least {@code capacity}
	 * bytes.
	 *
	 * @param capacity  the initial required capacity, must not be negative
	 *
	 * @throws IllegalArgumentException  if {@code capacity} is negative
	 */
	public ByteStringBuilder(int capacity) {
		this(new byte[idealCapacity(Check.nonNegative(capacity))], 0);
	}

	/**
	 * Constructs a new empty {@code ByteStringBuilder}.
	 */
	public ByteStringBuilder() {
		this(new byte[DEFAULT_CAPACITY], 0);
	}

	/**
	 * Returns the current capacity of the builder.
	 *
	 * @return  the current capacity of the builder
	 */
	public int capacity() {
		return buffer.length;
	}

	/**
	 * Returns the number of bytes in this builder.
	 *
	 * @return  the number of bytes in this builder
	 */
	public int length() {
		return length;
	}

	/**
	 * Sets the number of bytes in this builder to {@code newLength}.
	 *
	 * <p>When {@code newLength} is lesser than the current length {@code len},
	 * drops the last {@code (len - newLength)} bytes from the constructed sequence.
	 * If {@code newLength} is greater than {@code len}, appends {@code newLength - len}
	 * zero bytes.</p>
	 *
	 * <p>No memory is freed when reducing the length of the sequence.</p>
	 *
	 * @param newLength  the new length, must not be negative
	 *
	 * @throws IndexOutOfBoundsException  if {@code newLength} is negative
	 */
	public void setLength(int newLength) {
		if (newLength < 0) throw new IndexOutOfBoundsException(Integer.toString(newLength));

		if (newLength < length) {
			length = newLength;
		}
		else if (newLength > length) {
			ensureCapacity(newLength);
			Arrays.fill(buffer, length, newLength, (byte) 0);
		}
	}

	/**
	 * Attempts to reduce the memory consumption of this builder by reducing its capacity
	 * to a smaller, yet still sufficient value.
	 */
	public void trimToSize() {
		int cap = idealCapacity(length);
		if (cap < capacity()) {
			buffer = resize(buffer, cap);
		}
	}

	/**
	 * Sets the byte at position {@code index} to {@code value}.
	 *
	 * @param index  the index of the byte to set
	 * @param value  the new value of the byte at position {@code index}
	 *
	 * @throws IndexOutOfBoundsException  if {@code index} is negative or greater than or equal
	 *                                    to the current buffer length
	 */
	public void setByteAt(int index, byte value) {
		if (index < 0 || index > length) {
			throw new IndexOutOfBoundsException(String.valueOf(index));
		}
		buffer[index] = value;
	}

	/**
	 * Appends the byte {@code b}.
	 *
	 * @param b  the byte to append
	 * @return  this builder
	 */
	public ByteStringBuilder append(byte b) {
		ensureCapacity(length + 1);
		buffer[length] = b;
		length += 1;
		return this;
	}

	/**
	 * Appends the contents of the byte array {@code array}. {@code off}
	 * is the offset in {@code array} to start the write from, and {@code len} is the
	 * number of bytes that should be written.
	 *
	 * <p>Throws an {@code IndexOutOfBoundsException} if {@code off} or {@code len}
	 * is negative, or if {@code (off + len)} is greater than {@code array.length}.</p>
	 *
	 * @param array  the byte array, must not be {@code null}
	 * @param off  offset in {@code array} to start from
	 * @param len  number of bytes to write
	 * @return  this builder
	 *
	 * @throws NullPointerException  if {@code array} is {@code null}
	 * @throws IndexOutOfBoundsException  if {@code off} or {@code len} is negative, or if
	 *                                    {@code (off + len)} is greater than {@code array.length}
	 */
	public ByteStringBuilder append(byte[] array, int off, int len) {
		if (off < 0 || len < 0 || (off + len) > array.length) {
			throw new IndexOutOfBoundsException("off=" + off + ", len=" + len);
		}

		if (len > 0) {
			ensureCapacity(length + len);
			System.arraycopy(array, off, buffer, length, len);
			length += len;
		}

		return this;
	}

	/**
	 * Appends the contents of the byte array {@code array}.
	 *
	 * @param array  the byte array to append, must not be {@code null}
	 * @return  this builder
	 *
	 * @throws NullPointerException  if {@code array} is {@code null}
	 */
	public ByteStringBuilder append(byte[] array) {
		return append(array, 0, array.length);
	}

	/**
	 * Appends the contents of the byte string {@code string}.
	 *
	 * @param string  the byte string to append, must not be {@code null}
	 * @return  this builder
	 *
	 * @throws NullPointerException  if {@code string} is {@code null}
	 */
	public ByteStringBuilder append(ByteString string) {
		return append(string.getBytes());
	}

	/**
	 * Appends a char sequence {@code charSequence} interpreted as a sequence
	 * of bytes using the specified {@code Charset}.
	 *
	 * @param charSequence  the char sequence to append, must not be {@code null}
	 * @param charset  the charset to use for encoding, must not be {@code null}
	 * @return  this builder
	 *
	 * @throws NullPointerException  if {@code string} is {@code null}
	 * @throws IllegalArgumentException  if {@code charset} cannot does not provide encoding
	 *                                   capability (see {@link Charset#canEncode()})
	 */
	public ByteStringBuilder append(CharSequence charSequence, Charset charset) {
		if (!charset.canEncode()) {
			throw new IllegalArgumentException("Charset cannot encode: " + charset.name());
		}

		// FIXME: inefficient, could be done more directly
		append(ByteString.of(charSequence.toString(), charset));
		return this;
	}

	/**
	 * Appends the char sequence {@code charSequence} interpreted as a sequence
	 * of bytes using the virtual machine's default charset (see {@link Charset#defaultCharset()}).
	 *
	 * @param charSequence  the char sequence to append, must not be {@code null}
	 * @return  this builder
	 *
	 * @throws NullPointerException  if {@code charSequence} is {@code null}
	 */
	public ByteStringBuilder append(CharSequence charSequence) {
		return append(charSequence, Charset.defaultCharset());
	}

	/**
	 * Returns a byte string consisting of the bytes in this builder.
	 *
	 * @return  a byte string with this builder's contents
	 */
	public ByteString toByteString() {
		return ByteString.copyOf(buffer, 0, length);
	}

	/**
	 * Returns the interpretation of this builder's bytes as a {@code java.lang.String}.
	 *
	 * @return  a {@code java.lang.String} interpretation of the bytes in this builder
	 */
	@Override
	public String toString() {
		return toByteString().toString();
	}

}
