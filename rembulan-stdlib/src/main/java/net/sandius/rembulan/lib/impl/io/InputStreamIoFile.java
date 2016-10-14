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

package net.sandius.rembulan.lib.impl.io;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.lib.impl.IoFile;
import net.sandius.rembulan.util.Check;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamIoFile extends IoFile {

	private final SeekableInputStream in;

	public InputStreamIoFile(InputStream in, Table metatable, Object userValue) {
		super(metatable, userValue);
		this.in = new SeekableInputStream(Check.notNull(in));
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public void close() throws IOException {
		throw new UnsupportedOperationException("cannot close standard file");
	}

	@Override
	public void flush() throws IOException {
		// no-op
	}

	@Override
	public void write(ByteString s) throws IOException {
		throw new UnsupportedOperationException("Bad file descriptor");
	}

	@Override
	public long seek(IoFile.Whence whence, long offset) throws IOException {
		switch (whence) {
			case BEGINNING:
			case END:
				return in.setPosition(offset);

			case CURRENT_POSITION:
				return in.addPosition(offset);

			default: throw new IllegalArgumentException("Illegal whence: " + whence);
		}
	}

}
