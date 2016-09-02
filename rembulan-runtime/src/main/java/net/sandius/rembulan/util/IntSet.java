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

public class IntSet extends IntContainer {

	private final int[] values;

	private IntSet(int[] values) {
		// values must be sorted
		this.values = Check.notNull(values);
	}

	public static IntSet empty() {
		return new IntSet(new int[0]);
	}

	public static IntSet singleton(int i) {
		return new IntSet(new int[] { i });
	}

	public static IntSet from(int[] values) {
		Check.notNull(values);

		if (values.length == 0) {
			return empty();
		}
		else if (values.length == 1) {
			return singleton(values[0]);
		}
		else {
			// sort
			int[] sorted = Arrays.copyOf(values, values.length);
			Arrays.sort(sorted);

			// skip duplicates in place
			int uniq = 1;
			for (int i = 1; i < sorted.length; i++) {
				if (sorted[i] != sorted[i - 1]) {
					sorted[uniq] = sorted[i];
					uniq++;
				}
			}

			return new IntSet(Arrays.copyOf(sorted, uniq));
		}
	}

	public static IntSet from(IntIterable iterable) {
		return empty().plus(iterable);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IntSet intSet = (IntSet) o;
		return Arrays.equals(values, intSet.values);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}

	@Override
	public String toString() {
		return toString(",");
	}

	public String toString(String separator) {
		StringBuilder bld = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			bld.append(values[i]);
			if (i + 1 < values.length) {
				bld.append(separator);
			}
		}
		return bld.toString();
	}

	@Override
	public int length() {
		return size();
	}

	@Override
	public int get(int index) {
		return values[index];
	}

	public int size() {
		return values.length;
	}

	@Override
	public boolean contains(int i) {
		return Arrays.binarySearch(values, i) >= 0;
	}

	public IntSet minus(int i) {
		int idx = Arrays.binarySearch(values, i);
		if (idx >= 0) {
			int[] newValues = new int[values.length - 1];
			System.arraycopy(values, 0, newValues, 0, idx);  // x < i
			System.arraycopy(values, idx + 1, newValues, idx, values.length - idx - 1);  // x > i
			return new IntSet(newValues);
		}
		else {
			return this;
		}
	}

	public IntSet minus(IntContainer that) {
		Check.notNull(that);

		IntSet result = this;
		for (int i = 0; i < that.length(); i++) {
			// FIXME: this is very inefficient for IntSets -- that can be specialised
			result = result.minus(that.get(i));
		}
		return result;
	}

	public IntSet plus(int i) {
		int idx = Arrays.binarySearch(values, i);
		if (idx < 0) {
			int[] newValues = new int[values.length + 1];

			int insIdx = -idx - 1;
			System.arraycopy(values, 0, newValues, 0, insIdx);  // x < i
			System.arraycopy(values, insIdx, newValues, insIdx + 1, values.length - insIdx);  // x > i
			newValues[insIdx] = i;

			return new IntSet(newValues);
		}
		else {
			return this;
		}
	}

	public IntSet plus(IntIterable that) {
		Check.notNull(that);

		IntSet result = this;

		// FIXME: this is very inefficient for IntSets -- that can be specialised
		IntIterator it = that.iterator();
		while (it.hasNext()) {
			result = result.plus(it.next());
		}

		return result;
	}

	@Override
	public IntIterator iterator() {
		return new ArrayIntIterator(values);
	}

}
