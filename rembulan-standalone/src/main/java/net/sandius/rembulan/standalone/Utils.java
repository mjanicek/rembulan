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

package net.sandius.rembulan.standalone;

import net.sandius.rembulan.util.Check;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

abstract class Utils {

	private Utils() {
		// not to be instantiated or extended
	}

	public static String bytesToString(byte[] bytes) {
		// Lua likes clean 8-bit input; FIXME: is ISO-8859-1 okay?
		Charset charset = Charset.forName("ISO-8859-1");
		return new String(bytes, charset);
	}

	public static String readFile(String fileName) throws IOException {
		Check.notNull(fileName);
		return bytesToString(Files.readAllBytes(Paths.get(fileName)));
	}

	public static String readInputStream(InputStream stream) throws IOException {
		// FIXME: this ia a quick-and-dirty hack

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int b;
		do {
			b = stream.read();
			if (b != -1) {
				baos.write(b);
			}
		} while (b != -1);

		return bytesToString(baos.toByteArray());
	}

}
