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

	public void copyToArray(int[] array, int offset, int length) {
		Check.notNull(array);
		Check.nonNegative(offset);
		System.arraycopy(values, 0, array, offset, length);
	}

	public void copyToArray(int[] array, int offset) {
		copyToArray(array, offset, values.length);
	}

	public int[] copyToNewArray() {
		int[] cp = new int[values.length];
		copyToArray(cp, 0);
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
