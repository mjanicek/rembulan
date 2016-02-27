package net.sandius.rembulan.util;

public class ArrayIntIterator implements IntIterator {

	private final int[] array;
	private int index;

	public ArrayIntIterator(int[] array) {
		this.array = Check.notNull(array);
		this.index = 0;
	}

	@Override
	public boolean hasNext() {
		return index < array.length;
	}

	@Override
	public int next() {
		return array[index++];
	}

}
