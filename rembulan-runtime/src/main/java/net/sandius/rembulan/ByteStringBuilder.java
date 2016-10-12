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

public class ByteStringBuilder {

	private final ByteArrayOutputStream baos;

	public ByteStringBuilder() {
		this.baos = new ByteArrayOutputStream();
	}

	public ByteStringBuilder append(byte b) {
		baos.write(b);
		return this;
	}

	public ByteStringBuilder append(ByteString string) {
		try {
			string.writeTo(baos);
		}
		catch (IOException ex) {
			// should never happen!
			throw new RuntimeException(ex);
		}
		return this;
	}

	public ByteStringBuilder append(String string) {
		append(ByteString.of(string));
		return this;
	}

	public ByteString toByteString() {
		return ByteString.wrap(baos.toByteArray());
	}

	@Override
	public String toString() {
		return toByteString().toString();
	}

}
