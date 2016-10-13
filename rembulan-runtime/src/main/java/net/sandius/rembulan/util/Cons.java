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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * An immutable cons list.
 *
 * @param <T>  element type
 */
public class Cons<T> {

	/**
	 * The head of the list.
	 */
	public final T car;

	/**
	 * The tail of the list.
	 */
	public final Cons<T> cdr;

	/**
	 * Constructs a new cons list.
	 *
	 * @param car  head of the list, must not be {@code null}
	 * @param cdr  tail of the list, may be {@code null}
	 *
	 * @throws NullPointerException  if {@code car} is {@code null}
	 */
	public Cons(T car, Cons<T> cdr) {
		this.car = Objects.requireNonNull(car);
		this.cdr = cdr;
	}

	/**
	 * Constructs a new list consisting of a single element.
	 *
	 * @param car  the singleton element of the list, must not be {@code null}
	 */
	public Cons(T car) {
		this(car, null);
	}

	/**
	 * Creates a new cons list from the contents of the array {@code elems}.
	 *
	 * @param elems  array to build the list from, must not be {@code null}
	 * @param <T>  element type
	 * @return  a new cons list containing the contents of {@code elems} (possibly {@code null})
	 *
	 * @throws NullPointerException  if {@code elems} is {@code null}, or any element
	 *                               in {@code elems} is {@code null}
	 */
	public static <T> Cons<T> fromArray(T[] elems) {
		Cons<T> car = null;
		for (int i = elems.length - 1; i >= 0; i--) {
			car = new Cons<>(elems[i], car);
		}
		return car;
	}

	/**
	 * Creates a new cons list from the given arguments.
	 *
	 * @param elems  elements to build the list from, must not be {@code null}
	 * @param <T>  element type
	 * @return  a new cons list containing the contents of {@code elems} (possibly {@code null}
	 *
	 * @throws NullPointerException  if {@code elems} is {@code null}, or any element
	 *                               in {@code elems} is {@code null}
	 */
	public static <T> Cons<T> fromArgs(T... elems) {
		return fromArray(elems);
	}

	/**
	 * Creates a new cons list from the list {@code list}.
	 *
	 * @param list  list to build the cons list from, must not be {@code null}
	 * @param <T>  element type
	 * @return  a new cons list containing the contents of {@code elems} (possibly {@code null})
	 *
	 * @throws NullPointerException  if {@code list} is {@code null}, or any element
	 *                               in {@code list} is {@code null}
	 */
	public static <T> Cons<T> fromList(List<T> list) {
		return fromIterator(list.iterator());
	}

	private static <T> Cons<T> fromIterator(Iterator<T> iterator) {
		if (iterator.hasNext()) {
			return new Cons<T>(iterator.next(), fromIterator(iterator));
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the string representation of the list {@code cons}, using the {@code toString()}
	 * method to convert each element of {@code cons} to string and inserting {@code separator}
	 * between each pair.
	 *
	 * @param cons  the cons list, may be {@code null} (i.e., empty)
	 * @param separator  the separator, must not be {@code null}
	 * @return  a string representation of {@code cons}
	 *
	 * @throws NullPointerException  if {@code separator} is {@code null}
	 */
	public static String toString(Cons<?> cons, String separator) {
		Objects.requireNonNull(separator);

		StringBuilder bld = new StringBuilder();

		while (cons != null) {
			bld.append(cons.car.toString());
			if (cons.cdr != null) {
				bld.append(separator);
			}
			cons = cons.cdr;
		}

		return bld.toString();
	}

	/**
	 * Returns {@code true} iff this cons list is equal to the object {@code o}.
	 *
	 * <p>This cons list is equal to {@code o} iff {@code o} is a cons list of the same
	 * length as this cons list, and the elements of this cons list and {@code o}
	 * are pairwise equal (using {@code equals(Object)}).</p>
	 *
	 * @param o  the object to test for equality with this cons list
	 * @return  {@code true} iff {@code o} is a cons list with identical elements to this
	 *          cons list
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Cons<?> that = (Cons<?>) o;

		return car.equals(that.car) && !(cdr != null ? !cdr.equals(that.cdr) : that.cdr != null);
	}

	/**
	 * Returns the hash code of this cons list.
	 *
	 * <p>Note that this method traverses the entire list, and its time complexity is
	 * therefore O(<i>n</i>) with respect to the number of elements in the list.</p>
	 *
	 * @return  the hash code of this cons list
	 */
	@Override
	public int hashCode() {
		int result = car.hashCode();
		result = 31 * result + (cdr != null ? cdr.hashCode() : 0);
		return result;
	}

	/**
	 * Prepend the list {@code prefix} to this cons list.
	 *
	 * <p>The time and space complexity of this method is O(<i>m</i>), where <i>m</i> is the
	 * number of elements in {@code prefix}.</p>
	 *
	 * @param prefix  cons list to prepend to this cons list, may be {@code null} (i.e., empty)
	 * @return  this list with the contents of {@code prefix} prepended
	 */
	public Cons<T> prepend(Cons<T> prefix) {
		if (prefix == null) {
			return this;
		}
		else if (prefix.cdr == null) {
			return new Cons<T>(prefix.car, this);
		}
		else {
			return new Cons<T>(prefix.car, prepend(prefix.cdr));
		}
	}

	/**
	 * Returns the length of the cons list {@code cons}.
	 *
	 * <p>The time complexity of this method is O(<i>n</i>), where <i>n</i> is the
	 * number of elements in {@code cons}.</p>
	 *
	 * @param cons  cons list to get the length of, may be {@code null} (i.e., empty)
	 * @return  the length of {@code cons}
	 */
	public static int length(Cons<?> cons) {
		int i = 0;
		Cons<?> p = cons;

		while (p != null) {
			i += 1;
			p = p.cdr;
		}

		return i;
	}

	/**
	 * Returns the {@code idx}-th element in the cons list {@code cons}, or {@code null}
	 * if there is no element with such an index in {@code cons}.
	 *
	 * <p>The time complexity of this method is O({@code idx}).</p>
	 *
	 * @param cons  the cons list, may be {@code null} (i.e., empty)
	 * @param idx  the index, may be any value
	 * @param <T>  element type
	 * @return  the {@code idx}-th element of {@code cons}, or {@code null} if no such
	 *          element exists
	 */
	public static <T> T get(Cons<T> cons, int idx) {
		if (idx < 0) {
			return null;
		}
		else {
			for (Cons<T> p = cons ; idx >= 0 && p != null; idx--, p = p.cdr) {
				if (idx == 0) return p.car;
			}
			return null;
		}
	}

	/**
	 * Returns the list {@code cons} without the first {@code num} elements.
	 *
	 * <p>The time complexity of this method is O({@code idx}).</p>
	 *
	 * @param cons  the cons list, may be {@code null} (i.e., empty)
	 * @param num  the number of elements to drop, must be non-negative
	 * @param <T>  element type
	 * @return  {@code cons} without the first {@code num} elements
	 *
	 * @throws IllegalArgumentException  if {@code num} is negative
	 */
	public static <T> Cons<T> drop(Cons<T> cons, int num) {
		if (num < 0) {
			throw new IllegalArgumentException("num must be non-negative");
		}

		Cons<T> p = cons;
		for ( ; num >= 0 && p != null; num--, p = p.cdr) {
			if (num == 0) return p;
		}

		return null;
	}

	/**
	 * Returns a cons list obtained by concatenating {@code left} and {@code right}.
	 *
	 * <p>The time and space complexity of this method is O(<i>m</i>), where <i>m</i> is the
	 * number of elements in {@code left}.</p>
	 *
	 * @param left  the left list, may be {@code null} (i.e., empty)
	 * @param right  the right list, may be {@code null} (i.e., empty)
	 * @param <T>  element type
	 * @return  a list consisting of all elements of {@code left} followed by {@code right}
	 */
	public static <T> Cons<T> concatenate(Cons<T> left, Cons<T> right) {
		if (left == null) return right;
		if (right == null) return left;
		return right.prepend(left);
	}

	/**
	 * Returns a new iterator on the cons list {@code cons}.
	 *
	 * @param cons  the cons list, may be {@code null} (i.e., empty)
	 * @param <T>  element type
	 * @return  an iterator on {@code cons}
	 */
	public static <T> Iterator<T> newIterator(Cons<T> cons) {
		return new ConsIterator<T>(cons);
	}

	/**
	 * Returns a cons list obtained by flattening the list of lists {@code original}.
	 *
	 * @param original  the list of lists, may be {@code null} (i.e., empty)
	 * @param <T>  element type
	 * @return  a cons list obtained by flattening {@code original}
	 */
	public static <T> Cons<T> flatten(Cons<Cons<T>> original) {
		ArrayList<T> tmp = new ArrayList<T>();
		Cons<Cons<T>> p = original;
		while (p != null) {
			Cons<T> q = p.car;
			while (q != null) {
				tmp.add(q.car);
				q = q.cdr;
			}
			p = p.cdr;
		}

		return fromList(tmp);
	}

	/**
	 * An iterator on a cons list.
	 *
	 * @param <T>  element type
	 */
	static class ConsIterator<T> implements Iterator<T> {

		private Cons<T> p;

		/**
		 * Constructs a new iterator with the starting position at {@code p}.
		 *
		 * @param p  the starting position, may be {@code null}
		 */
		public ConsIterator(Cons<T> p) {
			this.p = p;
		}

		@Override
		public boolean hasNext() {
			return p != null;
		}

		@Override
		public T next() {
			if (p != null) {
				T v = p.car;
				p = p.cdr;
				return v;
			}
			else {
				throw new NoSuchElementException();
			}
		}

		/**
		 * Throws an {@link UnsupportedOperationException}, since cons lists
		 * are immutable.
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
