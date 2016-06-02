package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaFormat;

public final class LFloat extends LNumber {

	private final double value;

	public LFloat(double value) {
		this.value = value;
	}

	public static LFloat valueOf(double value) {
		// TODO: caching
		return new LFloat(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof LFloat)) return false;
		LFloat that = (LFloat) o;
		return Double.compare(that.value, value) == 0;
	}

	@Override
	public int hashCode() {
		long temp = Double.doubleToLongBits(value);
		return (int) (temp ^ (temp >>> 32));
	}

	@Override
	public String toString() {
		return LuaFormat.toString(value);
	}

	@Override
	public boolean isFloat() {
		return true;
	}

	public boolean isNaN() {
		return Double.isNaN(value);
	}

	@Override
	public long longValue() {
		return (long) value;
	}

	@Override
	public long exactLongValue() {
		long l = (long) value;
		if ((double) l == value) {
			return l;
		}
		else {
			throw new NoIntegerRepresentationException();
		}
	}

	@Override
	public double doubleValue() {
		return value;
	}

	@Override
	public LInteger toInteger() {
		return LInteger.valueOf(longValue());
	}

	@Override
	public LInteger asExactInteger() {
		long l = (long) value;
		if ((double) l == value) {
			return LInteger.valueOf(l);
		}
		else {
			return null;
		}
	}

	@Override
	public LFloat toFloat() {
		return this;
	}

	@Override
	public LNumber add(LNumber that) {
		return LFloat.valueOf(this.doubleValue() + that.doubleValue());
	}

	@Override
	public LNumber sub(LNumber that) {
		return LFloat.valueOf(this.doubleValue() - that.doubleValue());
	}

	@Override
	public LNumber mul(LNumber that) {
		return LFloat.valueOf(this.doubleValue() * that.doubleValue());
	}

	@Override
	public LNumber mod(LNumber that) {
		return LFloat.valueOf(RawOperators.rawmod(this.doubleValue(), that.doubleValue()));
	}

	@Override
	public LNumber idiv(LNumber that) {
		return LFloat.valueOf(RawOperators.rawidiv(this.doubleValue(), that.doubleValue()));
	}

	@Override
	public LFloat unm() {
		return LFloat.valueOf(-value);
	}

	@Override
	public boolean eq(LNumber that) {
		return this.doubleValue() == that.doubleValue();
	}

	@Override
	public boolean lt(LNumber that) {
		return this.doubleValue() < that.doubleValue();
	}

	@Override
	public boolean le(LNumber that) {
		return this.doubleValue() <= that.doubleValue();
	}

}
