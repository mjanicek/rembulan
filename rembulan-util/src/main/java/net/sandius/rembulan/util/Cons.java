package net.sandius.rembulan.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Cons<T> {

	public final T car;
	public final Cons<T> cdr;

	public Cons(T car, Cons<T> cdr) {
		this.car = Check.notNull(car);
		this.cdr = cdr;
	}

	public Cons(T car) {
		this(car, null);
	}

	public static <T> Cons<T> fromArray(T[] elems) {
		Cons<T> car = null;
		for (int i = elems.length - 1; i >= 0; i--) {
			car = new Cons<T>(elems[i], car);
		}
		return car;
	}

	public static <T> Cons<T> fromArgs(T... elems) {
		return fromArray(elems);
	}

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

	public static String toString(Cons<?> cons, String separator) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Cons<?> that = (Cons<?>) o;

		return car.equals(that.car) && !(cdr != null ? !cdr.equals(that.cdr) : that.cdr != null);
	}

	@Override
	public int hashCode() {
		int result = car.hashCode();
		result = 31 * result + (cdr != null ? cdr.hashCode() : 0);
		return result;
	}

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

	public static int length(Cons<?> cons) {
		int i = 0;
		Cons<?> p = cons;

		while (p != null) {
			i += 1;
			p = p.cdr;
		}

		return i;
	}

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

	public static <T> Cons<T> concatenate(Cons<T> left, Cons<T> right) {
		if (left == null) return right;
		if (right == null) return left;
		return right.prepend(left);
	}

	public static <T> Iterator<T> newIterator(Cons<T> cons) {
		return new ConsIterator<T>(cons);
	}

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

	protected static class ConsIterator<T> implements Iterator<T> {
		private Cons<T> p;

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
				return null;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
