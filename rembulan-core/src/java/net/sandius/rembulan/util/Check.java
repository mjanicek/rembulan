package net.sandius.rembulan.util;

public abstract class Check {

	private Check() {
		// not to be instantiated
	}

	public static void notNull(Object o) {
		if (o == null) {
			throw new IllegalArgumentException();
		}
	}

}
