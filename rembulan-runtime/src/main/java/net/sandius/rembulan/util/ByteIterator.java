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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator over a sequence of bytes.
 *
 * <p>This class implements the {@link Iterator} interface. However, for performance reasons,
 * it is preferable to use its {@link #nextByte()} method directly in order to avoid
 * result boxing.</p>
 */
public interface ByteIterator extends Iterator<Byte> {

	/**
	 * Returns the next byte from the sequence and increments the byte position.
	 *
	 * <p>When there is no next byte to be read from the stream, this method throws
	 * a {@link NoSuchElementException}. Use {@link #hasNext()} to detect this condition.</p>
	 *
	 * @return  the next byte from the byte stream
	 *
	 * @throws NoSuchElementException  if there are no more bytes in the stream
	 */
	byte nextByte();

}
