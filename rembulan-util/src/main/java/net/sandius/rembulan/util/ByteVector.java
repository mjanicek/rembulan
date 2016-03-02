package net.sandius.rembulan.util;

import java.util.Arrays;

public class ByteVector {

	private final byte[] bytes;

	private ByteVector(byte[] bytes) {
		this.bytes = Check.notNull(bytes);
	}

	public static ByteVector wrap(byte[] bytes) {
		return new ByteVector(bytes);
	}

	public static ByteVector copyFrom(byte[] bytes) {
		return wrap(Arrays.copyOf(bytes, bytes.length));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ByteVector that = (ByteVector) o;

		return Arrays.equals(bytes, that.bytes);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(bytes);
	}

	public int size() {
		return bytes.length;
	}

	public byte get(int index) {
		return bytes[index];
	}

	public byte[] copyToNewArray() {
		return Arrays.copyOf(bytes, bytes.length);
	}

}
