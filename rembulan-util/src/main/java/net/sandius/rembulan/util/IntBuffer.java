package net.sandius.rembulan.util;

import java.util.Arrays;

public class IntBuffer {

	private int[] buf;
	private int len;

	public IntBuffer(int size) {
		buf = new int[size];
		len = 0;
	}

	public IntBuffer() {
		this(8);
	}

	public static IntBuffer of(int... values) {
		// TODO: allocate the array directly
		IntBuffer buffer = new IntBuffer();
		for (int i = 0; i < values.length; i++) {
			buffer.append(values[i]);
		}
		return buffer;
	}

	private void resize(int to) {
		Check.gt(to, len);
		int[] nbuf = new int[to];
		System.arraycopy(buf, 0, nbuf, 0, len);
		buf = nbuf;
	}

	public int length() {
		return len;
	}

	public boolean isEmpty() {
		return length() == 0;
	}

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

	public boolean contains(int value) {
		for (int i = 0; i < len; i++) {
			if (buf[i] == value) return true;
		}
		return false;
	}

	public void append(int value) {
		if (len + 1 >= buf.length) {
			resize(buf.length * 2);
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

}
