package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.math.BigDecimal;

public abstract class RawOperators {

	private RawOperators() {
		// not to be instantiated
	}

	public static boolean isFloatingPoint(Number n) {
		return n instanceof Double || n instanceof Float || n instanceof BigDecimal;
	}

	public static boolean isNaN(Object o) {
		return o instanceof Number && Double.isNaN(((Number) o).doubleValue());
	}

	// Arithmetic operators

	// arguments must not be null
	public static Number rawadd(Number a, Number b) {
		if (isFloatingPoint(a) || isFloatingPoint(b)) {
			return a.doubleValue() + b.doubleValue();
		}
		else {
			return a.longValue() + b.longValue();
		}
	}

	// arguments must not be null
	public static Number rawsub(Number a, Number b) {
		if (isFloatingPoint(a) || isFloatingPoint(b)) {
			return a.doubleValue() - b.doubleValue();
		}
		else {
			return a.longValue() - b.longValue();
		}
	}

	// arguments must not be null
	public static Number rawmul(Number a, Number b) {
		if (isFloatingPoint(a) || isFloatingPoint(b)) {
			return a.doubleValue() * b.doubleValue();
		}
		else {
			return a.longValue() * b.longValue();
		}
	}

	// arguments must not be null
	public static Number rawdiv(Number a, Number b) {
		return a.doubleValue() / b.doubleValue();
	}

	// arguments must not be null
	public static Number rawmod(Number a, Number b) {
		if (isFloatingPoint(a) || isFloatingPoint(b)) {
			double x = a.doubleValue();
			double y = b.doubleValue();
			return y != 0 ? x - y * Math.floor(x / y) : Double.NaN;
		}
		else {
			long x = a.longValue();
			long y = b.longValue();
			if (y == 0) throw new IllegalArgumentException("attempt to perform 'n%0");
			else return x - y * (long) Math.floor((double) x / (double) y);
		}
	}

	// arguments must not be null
	public static Number rawidiv(Number a, Number b) {
		if (isFloatingPoint(a) || isFloatingPoint(b)) {
			return Math.floor(a.doubleValue() / b.doubleValue());
		}
		else {
			long x = a.longValue();
			long y = b.longValue();
			if (y == 0) throw new IllegalArgumentException("attempt to divide by zero");
			else return (long) Math.floor((double) x / (double) y);
		}
	}

	// arguments must not be null
	public static Number rawpow(Number a, Number b) {
		return Math.pow(a.doubleValue(), b.doubleValue());
	}

	public static Number rawunm(Number n) {
		if (isFloatingPoint(n)) {
			return -n.doubleValue();
		}
		else {
			return -n.longValue();
		}
	}

	// Bitwise operators

	public static Number rawband(long la, long lb) {
//		Long la = Conversions.numberAsLong(a);
//		Long lb = Conversions.numberAsLong(b);
//
//		if (la == null || lb == null) {
//			throw new NoIntegerRepresentationException();
//		}
//
		return la & lb;
	}

	public static Number rawbor(long la, long lb) {
//		Long la = Conversions.numberAsLong(a);
//		Long lb = Conversions.numberAsLong(b);
//
//		if (la == null || lb == null) {
//			throw new NoIntegerRepresentationException();
//		}

		return la | lb;
	}

	public static Number rawbxor(long la, long lb) {
//		Long la = Conversions.numberAsLong(a);
//		Long lb = Conversions.numberAsLong(b);
//
//		if (la == null || lb == null) {
//			throw new NoIntegerRepresentationException();
//		}

		return la ^ lb;
	}

	public static Number rawbnot(long ln) {
//		Long ln = Conversions.numberAsLong(n);
//
//		if (ln == null) {
//			throw new NoIntegerRepresentationException();
//		}

		return ~ln;
	}

	public static Number rawshl(long la, long lb) {
//		Long la = Conversions.numberAsLong(a);
//		Long lb = Conversions.numberAsLong(b);
//
//		if (la == null || lb == null) {
//			throw new NoIntegerRepresentationException();
//		}

		return la << lb;
	}

	public static Number rawshr(long la, long lb) {
//		Long la = Conversions.numberAsLong(a);
//		Long lb = Conversions.numberAsLong(b);
//
//		if (la == null || lb == null) {
//			throw new NoIntegerRepresentationException();
//		}

		return la >>> lb;
	}

	// Comparison operators

	public static boolean raweq(Object a, Object b) {
		if (a == null && b == null) {
			// two nils
			return true;
		}
		else if (a == null) {
			// b is definitely not nil; also ensures that neither a nor b is null in the tests below
			return false;
		}
		else if (a instanceof Number && b instanceof Number) {
			Number na = (Number) a;
			Number nb = (Number) b;
			// must denote the same mathematical value
			return na.doubleValue() == nb.doubleValue() && na.longValue() == nb.longValue();
		}
		else if (a instanceof Boolean || a instanceof String || a instanceof Function) {
			// value-based equality
			return a.equals(b);
		}
		else if (a instanceof Table
				|| a instanceof Thread
				|| a instanceof Userdata
				|| Value.isLightUserdata(a)) {

			// reference-based equality
			return a == b;
		}
		else {
			return false;
		}
	}

	public static boolean rawlt(Number a, Number b) {
		return isFloatingPoint(a) || isFloatingPoint(b)
			? a.doubleValue() < b.doubleValue()
			: a.longValue() < b.longValue();
	}

	public static boolean rawlt(String a, String b) {
		return a.compareTo(b) < 0;
	}

	public static boolean rawle(Number a, Number b) {
		return isFloatingPoint(a) || isFloatingPoint(b)
			? a.doubleValue() <= b.doubleValue()
			: a.longValue() <= b.longValue();
	}

	public static boolean rawle(String a, String b) {
		return a.compareTo(b) <= 0;
	}

	// Logical operators

	public static int stringLen(String s) {
		Check.notNull(s);
		return s.getBytes().length;  // FIXME: probably wasteful!
	}

}
