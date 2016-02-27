package net.sandius.rembulan.util;

public abstract class IntContainer implements IntIterable {

	public String toString(String separator) {
		StringBuilder bld = new StringBuilder();
		for (int i = 0; i < length(); i++) {
			bld.append(get(i));
			if (i + 1 < length()) {
				bld.append(separator);
			}
		}
		return bld.toString();
	}

	public abstract int length();

	public abstract int get(int index);

	public boolean contains(int value) {
		for (int i = 0; i < length(); i++) {
			if (get(i) == value) return true;
		}
		return false;
	}

	public boolean isEmpty() {
		return length() == 0;
	}

	@Override
	public IntIterator iterator() {
		return new Iterator();
	}

	protected class Iterator implements IntIterator {

		private int index;

		private Iterator() {
			this.index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < length();
		}

		@Override
		public int next() {
			return get(index++);
		}

	}

}
