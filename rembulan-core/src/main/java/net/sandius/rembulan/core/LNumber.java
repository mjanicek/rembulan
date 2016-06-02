package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaFormat;

public final class LNumber {

	public static final LNumber INTEGER_ZERO = new LNumber(false, 0);

	private final boolean isFloat;
	private final long bits;

	private LNumber(boolean isFloat, long bits) {
		this.isFloat = isFloat;
		this.bits = bits;
	}

	public static LNumber valueOf(double value) {
		return new LNumber(true, Double.doubleToRawLongBits(value));
	}

	private static class IntegerCache {

		// the cache array should be small enough to remain paged, and the test whether
		// a value is cached should be cheap
		static final int MIN = -256;
		static final int MAX = 255;

		private IntegerCache() {
			// not to be instantiated
		}

		private static final LNumber[] values;

		static {
			values = new LNumber[MAX - MIN + 1];
			for (int i = MIN; i <= MAX; i++) {
				values[i - MIN] = new LNumber(false, i);
			}
		}
	}

	public static LNumber valueOf(long value) {
		return (value >= IntegerCache.MIN && value <= IntegerCache.MAX)
				? IntegerCache.values[(int) value - IntegerCache.MIN]
				: new LNumber(false, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LNumber)) return false;

		LNumber that = (LNumber) o;
		if (this.isFloat != that.isFloat) return false;

		if (this.isFloat) {
			long a = Double.doubleToLongBits(Double.longBitsToDouble(this.bits));
			long b = Double.doubleToLongBits(Double.longBitsToDouble(that.bits));
			return a == b;
		}
		else {
			return this.bits == that.bits;
		}
	}

	@Override
	public int hashCode() {
		return 31 * (isFloat ? 1 : 0) + (int) (bits ^ (bits >>> 32));
	}

	@Override
	public String toString() {
		return isFloat ? LuaFormat.toString(doubleValue()) : LuaFormat.toString(longValue());
	}

	public boolean isFloat() {
		return isFloat;
	}

	public boolean isNaN() {
		return isFloat && Double.isNaN(doubleValue());
	}

	// may lose precision if not a long
	public long longValue() {
		return isFloat ? (long) Double.longBitsToDouble(bits) : bits;
	}

	// to long value, or throw NoIntegerRepresentation if the number has no integer representation
	public long exactLongValue() {
		if (isFloat) {
			double d = doubleValue();
			long l = (long) d;
			if ((double) l == d) {
				return l;
			}
			else {
				throw new NoIntegerRepresentationException();
			}
		}
		else {
			return bits;
		}
	}

	// may lose precision if not a float
	public double doubleValue() {
		return isFloat ? Double.longBitsToDouble(bits) : (double) bits;
	}

	@Deprecated
	public int intValue() {
		return (int) longValue();
	}

	public LNumber toInteger() {
		return isFloat ? valueOf(longValue()) : this;
	}

	public LNumber asExactInteger() {
		if (isFloat) {
			double d = Double.longBitsToDouble(bits);
			long l = (long) d;
			return (double) l == d ? valueOf(l) : null;
		}
		else {
			return this;
		}
	}

	public LNumber toFloat() {
		return isFloat ? this : valueOf(doubleValue());
	}

	public static LNumber add(LNumber a, LNumber b) {
		return a.isFloat() || b.isFloat()
				? valueOf(a.doubleValue() + b.doubleValue())
				: valueOf(a.longValue() + b.longValue());
	}

	public static LNumber sub(LNumber a, LNumber b) {
		return a.isFloat() || b.isFloat()
				? valueOf(a.doubleValue() - b.doubleValue())
				: valueOf(a.longValue() - b.longValue());
	}

	public static LNumber mul(LNumber a, LNumber b) {
		return a.isFloat() || b.isFloat()
				? valueOf(a.doubleValue() * b.doubleValue())
				: valueOf(a.longValue() * b.longValue());
	}

	public static LNumber mod(LNumber a, LNumber b) {
		return a.isFloat() || b.isFloat()
				? valueOf(RawOperators.rawmod(a.doubleValue(), b.doubleValue()))
				: valueOf(RawOperators.rawmod(a.longValue(), b.longValue()));
	}

	public static LNumber div(LNumber a, LNumber b) {
		return valueOf(a.doubleValue() / b.doubleValue());
	}

	public static LNumber idiv(LNumber a, LNumber b) {
		return a.isFloat() || b.isFloat()
				? valueOf(RawOperators.rawidiv(a.doubleValue(), b.doubleValue()))
				: valueOf(RawOperators.rawidiv(a.longValue(), b.longValue()));
	}

	public static LNumber pow(LNumber a, LNumber b) {
		return valueOf(RawOperators.rawpow(a.doubleValue(), b.doubleValue()));
	}

	public static LNumber unm(LNumber n) {
		return n.isFloat() ? valueOf(-n.doubleValue()) : valueOf(-n.longValue());
	}

	public static LNumber band(LNumber a, LNumber b) {
		return valueOf(a.exactLongValue() & b.exactLongValue());
	}

	public static LNumber bor(LNumber a, LNumber b) {
		return valueOf(a.exactLongValue() | b.exactLongValue());
	}

	public static LNumber bxor(LNumber a, LNumber b) {
		return valueOf(a.exactLongValue() ^ b.exactLongValue());
	}

	public static LNumber bnot(LNumber n) {
		return valueOf(~n.exactLongValue());
	}

	public static LNumber shl(LNumber a, LNumber b) {
		return valueOf(a.exactLongValue() << b.exactLongValue());
	}

	public static LNumber shr(LNumber a, LNumber b) {
		return valueOf(a.exactLongValue() >>> b.exactLongValue());
	}

	public static boolean eq(LNumber a, LNumber b) {
		return a.isFloat() || b.isFloat()
				? a.doubleValue() == b.doubleValue()
				: a.longValue() == b.longValue();
	}

	public static boolean lt(LNumber a, LNumber b) {
		return a.isFloat() || b.isFloat()
				? a.doubleValue() < b.doubleValue()
				: a.longValue() < b.longValue();
	}

	public static boolean le(LNumber a, LNumber b) {
		return a.isFloat() || b.isFloat()
				? a.doubleValue() <= b.doubleValue()
				: a.longValue() <= b.longValue();
	}

}
