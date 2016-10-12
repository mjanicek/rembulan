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

package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.ByteStringBuilder;
import net.sandius.rembulan.LuaFormat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StringLiteral extends Literal {

	private final ByteString value;

	private StringLiteral(ByteString value) {
		this.value = value;
	}

	private static int decValueOf(int c) {
		return c >= '0' && c <= '9' ? c - (int) '0' : -1;
	}

	private static int hexValueOf(int c) {
		return c >= '0' && c <= '9'
				? c - (int) '0'
				: (c >= 'a' && c <= 'f'
						? 10 + c - (int) 'a'
						: (c >= 'A' && c <= 'F'
								? 10 + c - (int) 'A'
								: -1));
	}

	private static boolean isWhitespace(int c) {
		return c <= 0xff && Character.isWhitespace(c);
	}

	private static void appendBytes(ByteStringBuilder builder, CharsetEncoder encoder, int codePoint) {
		final ByteBuffer bytes;
		try {
			bytes = encoder.encode(CharBuffer.wrap(Character.toChars(codePoint)));
		}
		catch (CharacterCodingException ex) {
			// should not happen
			throw new IllegalStateException(ex);
		}

		while (bytes.hasRemaining()) {
			builder.append(bytes.get());
		}
	}

	private static ByteString stringValueOf(InputStream in) throws IOException {
		Objects.requireNonNull(in);
		BufferedInputStream stream = new BufferedInputStream(in);

		// encoder for UTF-8 byte sequences
		CharsetEncoder utf8Encoder = StandardCharsets.UTF_8.newEncoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);

		ByteStringBuilder bld = new ByteStringBuilder();

		final int qmark;

		// skip the surrounding quotation marks
		if ((qmark = stream.read()) == -1) throw new IllegalArgumentException("string is empty");

		// the current byte position from the beginning of the string, incl. the quotation mark
		int pos = 1;

		int c;
		while ((c = stream.read()) != -1) {
			pos += 1;

			if (c == '\\') {
				// escaped character

				// index of the '\\' character
				final int oldPos = pos - 1;

				c = stream.read();

				if (decValueOf(c) != -1) {
					// decimal char specification: at most three such characters

					int dec = decValueOf(c);

					int rem = 2;
					do {
						stream.mark(rem);
						c = stream.read();
						pos += 1;

						int digit = decValueOf(c);
						if (digit != -1) {
							dec = dec * 10 + digit;
							rem -= 1;
						}
						else {
							// that was it
							stream.reset();
							pos -= 1;  // update position
							break;
						}
					} while (rem > 0);

					if (dec > 255) {
						throw new IllegalArgumentException("decimal escape too large at index " + oldPos);
					}

					// TODO: update position

					bld.append((byte) dec);
				}
				else if (c == 'x') {
					// hexadecimal escape

					int hex = 0;
					for (int j = 0; j < 2; j++) {
						c = stream.read();
						pos += 1;

						int digit = hexValueOf(c);

						if (digit == -1) {
							throw new IllegalArgumentException("hexadecimal digit expected at index " + pos);
						}
						else {
							hex = (hex << 4) + digit;
						}
					}

					bld.append((byte) hex);
				}
				else if (c == 'u') {
					if (stream.read() != '{') {
						throw new IllegalArgumentException("missing '{' at index " + (oldPos + 2));
					}

					pos += 1;  // the opening '{' has been read

					int value;

					{
						c = stream.read();
						pos += 1;

						int digit = hexValueOf(c);
						if (digit == -1) {
							throw new IllegalArgumentException("hexadecimal digit expected at index " + (pos - 1));
						}
						value = digit;
					}

					do {
						c = stream.read();
						pos += 1;

						int digit = hexValueOf(c);
						if (c != '}') {
							if (digit == -1) {
								throw new IllegalArgumentException("hexadecimal digit expected at index " + (pos - 1));
							}
							else {
								value = (value << 4) + digit;

								if (value >= Character.MAX_CODE_POINT) {
			                        throw new IllegalArgumentException("UTF-8 value too large at index " + (pos - 1));
								}
							}
						}
					} while (c != '}');

					appendBytes(bld, utf8Encoder, value);
				}
				else if (c == 'z') {
					// skip subsequent whitespace
					while (isWhitespace(stream.read())) {
						pos += 1;
					}
				}
				else if (c == '\n' || c == '\r') {
					if (c == '\r') {
						// this must be a \r\n
						if (stream.read() != '\n') {
							throw new IllegalArgumentException("\\n expected at index " + oldPos);
						}
						else {
							pos += 1;
						}
					}

					bld.append((byte) '\n');
				}
				else {
					final char d;
					switch (c) {
						case 'a': d = LuaFormat.CHAR_BELL; break;
						case 'b': d = '\b'; break;
						case 'f': d = '\f'; break;
						case 'n': d = '\n'; break;
						case 'r': d = '\r'; break;
						case 't': d = '\t'; break;
						case 'v': d = LuaFormat.CHAR_VERTICAL_TAB; break;
						case '\\': d = '\\'; break;
						case '\'': d = '\''; break;
						case '"': d = '"'; break;
						default:
							throw new IllegalArgumentException("invalid escape sequence at index " + oldPos + " (\\" + c + ")");
					}
					bld.append((byte) d);
				}
			}
			else {
				if (c != qmark) {
					bld.append((byte) c);
				}
			}
		}

		return bld.toByteString();
	}

	private static ByteString stringValueOf(ByteString s) {
		try {
			return stringValueOf(s.asInputStream());
		}
		catch (IOException ex) {
			// should not happen!
			throw new RuntimeException(ex);
		}
	}

	public static StringLiteral fromString(ByteString s) {
		return new StringLiteral(stringValueOf(s));
	}

	@Deprecated
	public static StringLiteral fromString(String s) {
		return fromString(ByteString.of(s));
	}

	// TODO: use a ByteString parameter
	public static StringLiteral verbatim(String s) {
		return new StringLiteral(ByteString.of(s));
	}

	public static StringLiteral fromName(Name n) {
		return new StringLiteral(ByteString.of(n.value()));
	}

	public ByteString value() {
		return value;
	}

	@Override
	public Literal accept(Transformer tf) {
		return tf.transform(this);
	}

}
