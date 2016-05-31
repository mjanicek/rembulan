package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaFormat;

public final class LInteger extends LNumber {

	private final long value;

	public LInteger(long value) {
		this.value = value;
	}

	public static LInteger valueOf(long value) {
		// TODO: caching
		return new LInteger(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof LInteger)) return false;
		LInteger that = (LInteger) o;
		return value == that.value;
	}

	@Override
	public int hashCode() {
		return (int) (value ^ (value >>> 32));
	}

	@Override
	public String toString() {
		return LuaFormat.toString(value);
	}

	@Override
	public boolean isFloat() {
		return false;
	}

	@Override
	public boolean isNaN() {
		return false;
	}

	@Override
	public long longValue() {
		return value;
	}

	@Override
	public double doubleValue() {
		return (double) value;
	}

	@Override
	public long exactLongValue() {
		return value;
	}

	@Override
	public LInteger toInteger() {
		return this;
	}

	@Override
	public LFloat toFloat() {
		return LFloat.valueOf(doubleValue());
	}

	@Override
	public LNumber add(LNumber that) {
		if (that instanceof LInteger) {
			return LInteger.valueOf(this.longValue() + that.longValue());
		}
		else {
			return LFloat.valueOf(this.doubleValue() + that.doubleValue());
		}
	}

	@Override
	public LNumber sub(LNumber that) {
		if (that instanceof LInteger) {
			return LInteger.valueOf(this.longValue() - that.longValue());
		}
		else {
			return LFloat.valueOf(this.doubleValue() - that.doubleValue());
		}
	}

	@Override
	public LNumber mul(LNumber that) {
		if (that instanceof LInteger) {
			return LInteger.valueOf(this.longValue() * that.longValue());
		}
		else {
			return LFloat.valueOf(this.doubleValue() * that.doubleValue());
		}
	}

	@Override
	public LNumber mod(LNumber that) {
		if (that instanceof LInteger) {
			return LInteger.valueOf(RawOperators.rawmod(this.longValue(), that.longValue()));
		}
		else {
			return LFloat.valueOf(RawOperators.rawmod(this.doubleValue(), that.doubleValue()));
		}
	}

	@Override
	public LNumber idiv(LNumber that) {
		if (that instanceof LInteger) {
			return LInteger.valueOf(RawOperators.rawidiv(this.longValue(), that.longValue()));
		}
		else {
			return LFloat.valueOf(RawOperators.rawidiv(this.doubleValue(), that.doubleValue()));
		}
	}

	@Override
	public LNumber unm() {
		return LInteger.valueOf(-value);
	}

	@Override
	public boolean eq(LNumber that) {
		if (that instanceof LInteger) {
			return this.longValue() == that.longValue();
		}
		else {
			return this.doubleValue() == that.doubleValue();
		}
	}

	@Override
	public boolean lt(LNumber that) {
		if (that instanceof LInteger) {
			return this.longValue() < that.longValue();
		}
		else {
			return this.doubleValue() < that.doubleValue();
		}
	}

	@Override
	public boolean le(LNumber that) {
		if (that instanceof LInteger) {
			return this.longValue() <= that.longValue();
		}
		else {
			return this.doubleValue() <= that.doubleValue();
		}
	}

}
