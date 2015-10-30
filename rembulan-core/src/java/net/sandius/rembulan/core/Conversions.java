package net.sandius.rembulan.core;

public abstract class Conversions {

	private Conversions() {
		// not to be instantiated
	}

	public static Long numberAsLong(Number n) {
		long l = n.longValue();
		return (double) l == n.doubleValue() && l != Long.MAX_VALUE ? l : null;
	}

	public static Number stringAsNumber(String s) {
		throw new UnsupportedOperationException();
	}

	// argument can be null
	public static Number objectAsNumber(Object o) {
		return o != null ? (o instanceof Number ? (Number) o : o instanceof String ? stringAsNumber((String) o) : null) : null;
	}

	public static Long objectAsLong(Object o) {
		Number n = objectAsNumber(o);
		return n != null ? numberAsLong(n) : null;
	}

	public static String objectAsString(Object o) {
		return o != null ? (o instanceof String ? (String) o
				: o instanceof Number ? RawOperators.toString((Number) o)
				: null) : null;
	}

	public static boolean objectToBoolean(Object o) {
		return !(o == null || (o instanceof Boolean && !((Boolean) o)));
	}

}
