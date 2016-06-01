package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaFormat;

public final class LInteger extends LNumber {

	public static final LInteger ZERO = new LInteger(0L);

	private final long value;

	private LInteger(long value) {
		this.value = value;
	}

	private static class Cache {

		// the cache array should be small enough to remain paged, and the test whether
		// a value is cached should be cheap
		static final int MIN = -256;
		static final int MAX = 255;

		private Cache() {
			// not to be instantiated
		}

		private static final LInteger[] values;

		static {
			values = new LInteger[MAX - MIN + 1];
			for (int i = MIN; i <= MAX; i++) {
				values[i - MIN] = new LInteger(i);
			}
		}
	}

	public static LInteger valueOf(long value) {
		return (value >= Cache.MIN && value <= Cache.MAX)
				? Cache.values[(int) value - Cache.MIN]
				: new LInteger(value);
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
	public LInteger asExactInteger() {
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
