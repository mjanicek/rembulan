package net.sandius.rembulan.util;

import java.util.ListIterator;

public class ReverseListIterator<E> implements ListIterator<E> {

	private final ListIterator<E> it;

	public ReverseListIterator(ListIterator<E> iterator) {
		Check.notNull(iterator);
		this.it = iterator;
	}

	@Override
	public boolean hasNext() {
		return it.hasPrevious();
	}

	@Override
	public E next() {
		return it.previous();
	}

	@Override
	public boolean hasPrevious() {
		return it.hasNext();
	}

	@Override
	public E previous() {
		return it.next();
	}

	@Override
	public int nextIndex() {
		return it.previousIndex();
	}

	@Override
	public int previousIndex() {
		return it.nextIndex();
	}

	@Override
	public void remove() {
		it.remove();
	}

	@Override
	public void set(E e) {
		it.set(e);
	}

	@Override
	public void add(E e) {
		it.add(e);
	}

}
