package net.sandius.rembulan.util;

public abstract class Check {

	private Check() {
		// not to be instantiated
	}

	public static <T> T notNull(T o) {
		if (o == null) {
			throw new IllegalArgumentException("argument is null");
		}
		return o;
	}

	public static void isNull(Object o) {
		if (o != null) {
			throw new IllegalArgumentException("argument is not null: " + o);
		}
	}

	public static <T> T isEq(T a, T b) {
		if (a != b) {
			throw new IllegalArgumentException("argument " + a + " is not equal to " + b);
		}
		return a;
	}

	public static int isEq(int a, int b) {
		if (a != b) {
			throw new IllegalArgumentException("integer " + a + " is not equal to " + b);
		}
		return a;
	}

	public static void numOfArgs(Object[] args, int num) {
		Check.notNull(args);

		if (args.length < num) {
			throw new IllegalArgumentException("bad argument #" + (args.length + 1) + ": value expected");
		}
	}

	public static int inRange(int n, int min, int max) {
		if (n < min && n > max) {
			throw new IllegalArgumentException("integer " + n + " out of range: [" + min + ", " + max + "]");
		}
		return n;
	}

	public static int lt(int n, int limit) {
		if (!(n < limit)) {
			throw new IllegalArgumentException("integer " + n + " is not lesser than " + limit);
		}
		return n;
	}

	public static long lt(long n, long limit) {
		if (!(n < limit)) {
			throw new IllegalArgumentException("long " + n + " is not lesser than " + limit);
		}
		return n;
	}

	public static int gt(int n, int limit) {
		if (!(n > limit)) {
			throw new IllegalArgumentException("integer " + n + " is not greater than " + limit);
		}
		return n;
	}

	public static long gt(long n, long limit) {
		if (!(n > limit)) {
			throw new IllegalArgumentException("long " + n + " is not greater than " + limit);
		}
		return n;
	}

	public static void isTrue(boolean b) {
		if (!b) {
			throw new IllegalArgumentException("condition is false");
		}
	}

	public static void isFalse(boolean b) {
		if (b) {
			throw new IllegalArgumentException("condition is true");
		}
	}

	public static int nonNegative(int n) {
		return gt(n, -1);
	}

	public static long nonNegative(long n) {
		return gt(n, -1);
	}

	public static int positive(int n) {
		return gt(n, 0);
	}

}
