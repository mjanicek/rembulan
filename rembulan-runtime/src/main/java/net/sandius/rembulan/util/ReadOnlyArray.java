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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class ReadOnlyArray<T> implements Iterable<T> {

	protected final T[] values;

	private ReadOnlyArray(T[] values) {
		this.values = Check.notNull(values);
	}

	public static <T> ReadOnlyArray<T> wrap(T[] values) {
		return new ReadOnlyArray<T>(values);
	}

	public static <T> ReadOnlyArray<T> copyFrom(T[] values) {
		Check.notNull(values);
		return wrap(Arrays.copyOf(values, values.length));
	}

	public static <T> ReadOnlyArray<T> fromCollection(Class<T> clazz, Collection<T> c) {
		Check.notNull(clazz);
		Check.notNull(c);

		@SuppressWarnings("unchecked")
		T[] values = (T[]) Array.newInstance(clazz, c.size());

		Iterator<T> it = c.iterator();
		int i = 0;
		while (it.hasNext()) {
			values[i++] = it.next();
		}

		return wrap(values);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ReadOnlyArray<?> that = (ReadOnlyArray<?>) o;

		return Arrays.deepEquals(this.values, that.values);
	}

	public boolean shallowEquals(ReadOnlyArray<T> that) {
		return this == that || that != null && Arrays.equals(this.values, that.values);
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode(values);
	}

	public int shallowHashCode() {
		return Arrays.hashCode(values);
	}

	public T get(int idx) {
		return values[idx];
	}

	public ReadOnlyArray<T> update(int idx, T value) {
		if (get(idx) == value) {
			return this;
		}
		else {
			T[] copy = Arrays.copyOf(values, values.length);
			copy[idx] = value;
			return wrap(copy);
		}
	}

	// FIXME: size or length?
	public int size() {
		return values.length;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public T[] copyToNewArray() {
		return Arrays.copyOf(values, values.length);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < values.length;
			}

			@Override
			public T next() {
				return values[i++];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public abstract static class Builder<T, U extends ReadOnlyArray<T>>
			implements GenericBuilder<U> {

		protected final ArrayList<T> values;

		public Builder() {
			this.values = new ArrayList<T>();
		}

		public Builder append(T v) {
			values.add(v);
			return this;
		}

		public T get(int idx) {
			return values.get(idx);
		}

		public Builder set(T[] vs) {
			values.clear();
			Collections.addAll(values, vs);
			return this;
		}

		public int size() {
			return values.size();
		}

		public boolean isEmpty() {
			return values.isEmpty();
		}

	}

}
