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

	public static void inRange(int n, int min, int max) {
		if (n < min && n > max) {
			throw new IllegalArgumentException("integer " + n + " out of range: [" + min + ", " + max + "]");
		}
	}

	public static void nonNegative(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("integer " + n + " must be non-negative");
		}
	}

	public static void positive(int n) {
		if (n < 1) {
			throw new IllegalArgumentException("integer " + n + " must be positive");
		}
	}

}
