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

import java.util.Arrays;

public class IntBuffer extends IntContainer {

	private static final int DEFAULT_EMPTY_CAPACITY = 8;
	private static final int GROW_FACTOR = 2;

	private int[] buf;
	private int len;

	private IntBuffer(int[] buf, int len) {
		this.buf = Check.notNull(buf);
		this.len = len;
	}

	private IntBuffer(int size) {
		this(new int[size], 0);
	}

	public static IntBuffer empty() {
		return new IntBuffer(DEFAULT_EMPTY_CAPACITY);
	}

	public static IntBuffer from(int[] values) {
		int len = values.length;

		int size = DEFAULT_EMPTY_CAPACITY;
		while (len > size) size *= GROW_FACTOR;

		int[] buf = new int[size];
		System.arraycopy(values, 0, buf, 0, values.length);

		return new IntBuffer(buf, len);
	}

	public static IntBuffer from(IntIterable iterable) {
		IntBuffer buf = empty();

		IntIterator it = iterable.iterator();
		while (it.hasNext()) {
			buf.append(it.next());
		}

		return buf;
	}

	public static IntBuffer of(int... values) {
		return from(values);
	}

	private void resizeTo(int to) {
		Check.gt(to, len);
		int[] nbuf = new int[to];
		System.arraycopy(buf, 0, nbuf, 0, len);
		buf = nbuf;
	}

	@Override
	public int length() {
		return len;
	}

	@Override
	public int get(int idx) {
		Check.inRange(idx, 0, len - 1);
		return buf[idx];
	}

	public void set(int idx, int value) {
		Check.inRange(idx, 0, len - 1);
		buf[idx] = value;
	}

	public void removeIndex(int idx) {
		Check.inRange(idx, 0, len - 1);
		System.arraycopy(buf, idx + 1, buf, idx, len - idx - 1);
		len -= 1;

		if (len < buf.length / GROW_FACTOR && buf.length / GROW_FACTOR > DEFAULT_EMPTY_CAPACITY) {
			resizeTo(buf.length / GROW_FACTOR);
		}
	}

	public void clear() {
		len = 0;
		resizeTo(DEFAULT_EMPTY_CAPACITY);
	}

	public void append(int value) {
		if (len + 1 >= buf.length) {
			resizeTo(buf.length * GROW_FACTOR);
		}

		buf[len++] = value;
	}

	public void append(IntBuffer that) {
		Check.notNull(that);

		// FIXME: ugly!
		for (int i = 0; i < that.length(); i++) {
			append(that.get(i));
		}
	}

	public IntVector toVector() {
		return IntVector.wrap(Arrays.copyOf(buf, len));
	}

	@Override
	public IntIterator iterator() {
		return new Iterator();
	}

	private class Iterator implements IntIterator {

		private int index;

		public Iterator() {
			this.index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < len;
		}

		@Override
		public int next() {
			Check.lt(index, buf.length);
			return buf[index++];
		}

	}

}
