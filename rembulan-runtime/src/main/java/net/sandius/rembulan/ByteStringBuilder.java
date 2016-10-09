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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A byte string builder, similar in functionality and purpose to {@link StringBuilder}.
 */
public class ByteStringBuilder {

	private final ByteArrayOutputStream baos;

	public ByteStringBuilder(int size) {
		this.baos = new ByteArrayOutputStream(size);
	}

	public ByteStringBuilder() {
		this(32);
	}

	public ByteStringBuilder append(char c) {
		baos.write((int) c & 0xff);
		return this;
	}

	public ByteStringBuilder append(int i) {
		return this.append(String.valueOf(i));
	}

	public ByteStringBuilder append(long l) {
		return this.append(String.valueOf(l));
	}

	public ByteStringBuilder append(float f) {
		return this.append(String.valueOf(f));
	}

	public ByteStringBuilder append(double d) {
		return this.append(String.valueOf(d));
	}

	public ByteStringBuilder append(String string) {
		return this.append(ByteString.viewOf(string));
	}

	public ByteStringBuilder append(String string, Charset charset) {
		return this.append(ByteString.encode(string, charset));
	}

	public ByteStringBuilder append(ByteString byteString) {
		try {
			byteString.writeTo(baos);
		}
		catch (IOException ex) {
			// should never happen!
			throw new RuntimeException(ex);
		}

		return this;
	}

	public ByteStringBuilder append(Object o) {
		return this.append(o.toString());
	}

	public ByteString toByteString() {
		return ByteString.wrap(baos.toByteArray());
	}

	public String toString(Charset charset) {
		return toByteString().toString(charset);
	}

	@Override
	public String toString() {
		return toByteString().toString();
	}

}
