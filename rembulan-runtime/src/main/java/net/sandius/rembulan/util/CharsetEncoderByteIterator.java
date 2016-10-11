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

package net.sandius.rembulan.util;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * An incremental charset encoder, encoding a string into bytes lazily.
 */
public class CharsetEncoderByteIterator implements ByteIterator {

	private String string;
	private CharsetEncoder encoder;

	// input buffer
	private final CharBuffer in;

	// output buffer
	private final ByteBuffer out;

	// index of the next character to read from the string
	private int idx;

	// index of the next byte that will be returned from the next successful call to nextByte()
	private int byteIdx;

	// true if we have encoded all characters, but haven't called flush() yet
	private boolean flushed;

	// convert at most 16 characters in every encoding step by default
	private static final int DEFAULT_STEP_SIZE = 16;

	/**
	 * Constructs a new encoder instance that iterates over {@code string}, converting
	 * it to bytes using the charset {@code charset}.
	 *
	 * <p>The encoder reads up to {@code stepSize} characters at the same time,
	 * buffering the results internally. {@code stepSize} must be at least 2 (this is to
	 * ensure that surrogate pairs are processed correctly).
	 *
	 * @param string  the string to iterate over, must not be {@code null}
	 * @param charset  the charset to use for encoding characters to bytes, must not be {@code null}
	 * @param stepSize  the number to characters to try encoding in each encoding step,
	 *                  must be positive
	 *
	 * @throws NullPointerException  if {@code string} or {@code charset} is {@code null}
	 * @throws IllegalArgumentException  if {@code stepSize} is lesser than 2
	 */
	public CharsetEncoderByteIterator(String string, Charset charset, int stepSize) {
		Objects.requireNonNull(string);
		Check.gt(stepSize, 1);

		// use the same settings as String.getBytes(Charset)
		this.encoder = charset.newEncoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE)
				.reset();

		this.string = string;
		this.idx = 0;
		this.byteIdx = 0;
		this.flushed = false;

		// no need to allocate more chars than what the string can give us
		stepSize = Math.min(stepSize, string.length());
		stepSize = Math.max(2, stepSize);  // but ensure we can always handle surrogate pairs

		this.in = CharBuffer.allocate(stepSize);

		int outBufferSize = (int) ((stepSize + 1) * encoder.maxBytesPerChar());
		this.out = ByteBuffer.allocate(outBufferSize);
		out.flip();
	}

	/**
	 * Constructs a new encoder instance that iterates over {@code string}, converting
	 * it to bytes using the charset {@code charset}.
	 *
	 * <p>To customise the number of characters encoded in every encoding step, use
	 * {@link #CharsetEncoderByteIterator(String, Charset, int)}. This method uses
	 * a reasonable default value for step size.</p>
	 *
	 * @param string  the string to iterate over, must not be {@code null}
	 * @param charset  the charset to use for encoding characters to bytes, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code string} or {@code charset} is {@code null}
	 */
	public CharsetEncoderByteIterator(String string, Charset charset) {
		this(string, charset, DEFAULT_STEP_SIZE);
	}

	private void fetch() {
		in.clear();
		while (idx < string.length() && in.hasRemaining()) {
			char c = string.charAt(idx);
			if (Character.isHighSurrogate(c) && in.remaining() < 2) {
				// not enough space to process the surrogate pair: process in the next pass
				break;
			}
			else {
				in.put(c);
				idx++;
			}
		}
		in.flip();
	}

	private void encode() {
		out.clear();
		try {
			CoderResult result = encoder.encode(in, out, idx >= string.length());
			if (!result.isUnderflow()) {
				// should not happen, but we want to know about it!
				result.throwException();
			}
		}
		catch (CharacterCodingException ex) {
			throw new IllegalStateException(ex);
		}

		out.flip();
	}

	private void encodeNextChars() {
		fetch();
		encode();
	}

	private void flush() {
		flushed = true;
		out.clear();
		try {
			CoderResult result = encoder.flush(out);
			if (!result.isUnderflow()) {
				result.throwException();
			}
		}
		catch (CharacterCodingException ex) {
			throw new IllegalStateException(ex);
		}

		out.flip();
	}

	/**
	 * Returns {@code true} iff the byte stream contains more bytes. In other words,
	 * returns {@code false} iff {@link #nextByte()} would throw a {@link NoSuchElementException}.
	 *
	 * @return  {@code true} if there are more bytes in the stream, {@code false} otherwise
	 */
	@Override
	public boolean hasNext() {
		if (out.hasRemaining()) {
			return true;
		}
		else {
			if (idx < string.length()) {
				encodeNextChars();
				return out.hasRemaining();
			}
			else if (!string.isEmpty() && !flushed) {
				flush();
				return out.hasRemaining();
			}
			else {
				return false;
			}
		}
	}

	@Override
	public int position() {
		return byteIdx;
	}

	@Override
	public byte nextByte() {
		try {
			byte b = out.get();
			byteIdx++;
			return b;
		}
		catch (BufferOverflowException ex) {
			throw new NoSuchElementException();
		}
	}

	/**
	 * Returns the next byte from the byte stream and increments the byte position.
	 *
	 * <p>In order to avoid boxing and unboxing the result, call {@link #nextByte()} directly.
	 * This method is provided in order to implement the {@link Iterator} interface.</p>
	 *
	 * @return  the next byte from the byte stream
	 *
	 * @throws NoSuchElementException  if there are no more bytes in the stream
	 */
	@Override
	public Byte next() {
		return Byte.valueOf(nextByte());
	}

	/**
	 * Always throws an {@link UnsupportedOperationException}, since this is a read-only iterator.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("read-only iterator");
	}

}
