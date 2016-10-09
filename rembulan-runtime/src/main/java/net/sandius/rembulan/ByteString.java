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

public abstract class ByteString implements CharSequence {

	ByteString() {
		// no-op, package-private to restrict access
	}

	/**
	 * Returns an 8-bit view of the string {@code s}.
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

	public static ByteString copyOf(byte[] bytes) {
		return wrap(Arrays.copyOf(bytes, bytes.length));
	}

	public static ByteString decode(String s) {
		return wrap(s.getBytes());
	}

	public static ByteString decode(String s, Charset charset) {
		return wrap(s.getBytes(charset));
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof ByteString && this.equals((ByteString) o);
	}

	protected abstract boolean equals(ByteString that);

	/**
	 * Returns a new byte array containing the byte view of the underlying string.
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

	public String toString(Charset charset) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(length());
		putTo(byteBuffer);
		byteBuffer.rewind();
		return charset.decode(byteBuffer).toString();
	}

}
