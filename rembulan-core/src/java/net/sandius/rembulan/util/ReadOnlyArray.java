package net.sandius.rembulan.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class ReadOnlyArray<T> implements Iterable<T> {

	protected final T[] values;

	public ReadOnlyArray(T[] values) {
		this.values = values;
	}

	public ReadOnlyArray(Class<T> clazz, Collection<T> values) {
		this.values = (T[]) Array.newInstance(clazz, values.size());

		Iterator<T> it = values.iterator();
		int i = 0;
		while (it.hasNext()) {
			this.values[i++] = it.next();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ReadOnlyArray<?> that = (ReadOnlyArray<?>) o;

		return Arrays.deepEquals(this.values, that.values);
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode(values);
	}

	public T get(int idx) {
		return values[idx];
	}

	public int size() {
		return values.length;
	}

	public boolean isEmpty() {
		return size() == 0;
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
