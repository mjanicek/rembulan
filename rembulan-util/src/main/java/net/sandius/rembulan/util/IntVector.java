package net.sandius.rembulan.util;

import java.util.Arrays;

// an immutable vector of ints
public class IntVector extends IntContainer {

	public static final IntVector EMPTY = new IntVector(new int[0]);

	private final int[] values;

	private IntVector(int[] values) {
		this.values = Check.notNull(values);
	}

	public static IntVector wrap(int[] array) {
		Check.notNull(array);
		return array.length > 0 ? new IntVector(array) : EMPTY;
	}

	public static IntVector copyFrom(int[] array) {
		Check.notNull(array);
		int[] values = new int[array.length];
		System.arraycopy(array, 0, values, 0, array.length);
		return wrap(values);
	}

	public int[] copyToNewArray() {
		int[] cp = new int[values.length];
		System.arraycopy(values, 0, cp, 0, values.length);
		return cp;
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

	@Override
	public int length() {
		return values.length;
	}

	@Override
	public int get(int index) {
		return values[index];
	}

	@Override
	public IntIterator iterator() {
		return new ArrayIntIterator(values);
	}

}
