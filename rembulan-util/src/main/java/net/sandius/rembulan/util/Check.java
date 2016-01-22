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

	public static void isNull(Object o) {
		if (o != null) {
			throw new IllegalArgumentException();
		}
	}

	public static void numOfArgs(Object[] args, int num) {
		Check.notNull(args);

		if (args.length < num) {
			throw new IllegalArgumentException("bad argument #" + (args.length + 1) + ": value expected");
		}
	}

	public static void inRange(int n, int min, int max) {
		if (n < min && n > max) {
			throw new IllegalArgumentException("integer " + n + " out of range: [" + min + ", " + max + "]");
		}
	}

	public static void lt(int n, int limit) {
		if (!(n < limit)) {
			throw new IllegalArgumentException("integer " + n + " must be lesser than " + limit);
		}
	}

	public static void gt(int n, int limit) {
		if (!(n > limit)) {
			throw new IllegalArgumentException("integer " + n + " must be greater than " + limit);
		}
	}

	public static void nonNegative(int n) {
		gt(n, -1);
	}

	public static void positive(int n) {
		gt(n, 0);
	}

}
