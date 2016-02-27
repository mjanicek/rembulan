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

	public IntBuffer(int size) {
		this(new int[size], 0);
	}

	public IntBuffer() {
		this(DEFAULT_EMPTY_CAPACITY);
	}

	public static IntBuffer from(int[] values) {
		int len = values.length;

		int size = DEFAULT_EMPTY_CAPACITY;
		while (len > size) size *= GROW_FACTOR;

		int[] buf = new int[size];
		System.arraycopy(values, 0, buf, 0, values.length);

		return new IntBuffer(buf, len);
	}

	public static IntBuffer of(int... values) {
		return from(values);
	}

	private void resize(int to) {
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
			resize(buf.length / GROW_FACTOR);
		}
	}

	public void removeValue(int value) {
		int i = 0;
		while (i < len) {
			if (buf[i] == value) {
				removeIndex(i);
			}
			else {
				i++;
			}
		}
	}

	public void replaceValue(int oldValue, int newValue) {
		for (int i = 0; i < len; i++) {
			if (buf[i] == oldValue) {
				buf[i] = newValue;
			}
		}
	}

	public void clear() {
		len = 0;
	}

	public void append(int value) {
		if (len + 1 >= buf.length) {
			resize(buf.length * GROW_FACTOR);
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
