package net.sandius.rembulan.util;

public abstract class IntContainer {

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

}
