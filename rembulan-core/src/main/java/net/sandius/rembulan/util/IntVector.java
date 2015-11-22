package net.sandius.rembulan.util;

import java.util.ArrayList;
import java.util.Arrays;

// an immutable vector of ints
public class IntVector {

	public static final IntVector EMPTY = new IntVector(new int[0]);

	private final int[] values;

	private IntVector(int[] values) {
		Check.notNull(values);
		this.values = values;
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

	public int length() {
		return values.length;
	}

	public int get(int index) {
		return values[index];
	}

	public static class Builder implements GenericBuilder<IntVector> {

		private final ArrayList<Integer> values;

		private Builder(ArrayList<Integer> values) {
			Check.notNull(values);
			this.values = values;
		}

		private Builder() {
			this(new ArrayList<Integer>());
		}

		public Builder copy() {
			ArrayList<Integer> cp = new ArrayList<Integer>();
			cp.addAll(values);
			return new Builder(cp);
		}

		public Builder clear() {
			values.clear();
			return this;
		}

		public Builder append(int v) {
			values.add(v);
			return this;
		}

		public Builder append(int[] vs) {
			Check.notNull(vs);
			for (int v : vs) {
				append(v);
			}
			return this;
		}

		public Builder set(int[] vs) {
			Check.notNull(vs);
			return clear().append(vs);
		}

		public Builder set(IntVector vs) {
			Check.notNull(vs);
			return clear().append(vs.copyToNewArray());
		}

		@Override
		public IntVector build() {
			int[] vs = new int[values.size()];

			for (int i = 0; i < values.size(); i++) {
				vs[i] = values.get(i);
			}

			return IntVector.wrap(vs);
		}

	}

	public static Builder newBuilder() {
		return new Builder();
	}

}
