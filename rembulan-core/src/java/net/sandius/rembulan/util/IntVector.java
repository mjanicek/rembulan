package net.sandius.rembulan.util;

import java.util.Arrays;

// an immutable vector of ints
public class IntVector {

	private final int[] values;

	private IntVector(int[] values) {
		Check.notNull(values);
		this.values = values;
	}

	public static IntVector wrap(int[] array) {
		Check.notNull(array);
		return new IntVector(array);
	}

	public static IntVector copyFrom(int[] array) {
		Check.notNull(array);
		int[] values = new int[array.length];
		System.arraycopy(array, 0, values, 0, array.length);
		return wrap(values);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IntVector intVector = (IntVector) o;

		return Arrays.equals(values, intVector.values);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}

	public int length() {
		return values.length;
	}

	public int get(int index) {
		return values[index];
	}

}
