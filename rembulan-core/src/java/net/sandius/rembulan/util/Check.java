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

	public static void numOfArgs(Object[] args, int num) {
		Check.notNull(args);

		if (args.length < num) {
			throw new IllegalArgumentException("bad argument #" + (args.length + 1) + ": value expected");
		}
	}

}
