package net.sandius.rembulan.core;

public abstract class LNumber {

	public abstract boolean isFloat();

	// may lose precision if not a long
	public abstract long longValue();

	// to long value, or throw NoIntegerRepresentation if the number has no integer representation
	public abstract long exactLongValue();

	// may lose precision if not a float
	public abstract double doubleValue();

	@Deprecated
	public int intValue() {
		return (int) longValue();
	}

	public abstract LInteger toInteger();

	public abstract LInteger asExactInteger();

	public abstract LFloat toFloat();

	public abstract LNumber add(LNumber that);

	public abstract LNumber sub(LNumber that);

	public abstract LNumber mul(LNumber that);

	public abstract LNumber mod(LNumber that);

	public LFloat div(LNumber that) {
		return LFloat.valueOf(this.doubleValue() / that.doubleValue());
	}

	public abstract LNumber idiv(LNumber that);

	public LFloat pow(LNumber that) {
		return LFloat.valueOf(RawOperators.rawpow(this.doubleValue(), that.doubleValue()));
	}

	public abstract LNumber unm();

	public LInteger band(LNumber that) {
		return LInteger.valueOf(this.exactLongValue() & that.exactLongValue());
	}

	public LInteger bor(LNumber that) {
		return LInteger.valueOf(this.exactLongValue() | that.exactLongValue());
	}

	public LInteger bxor(LNumber that) {
		return LInteger.valueOf(this.exactLongValue() ^ that.exactLongValue());
	}

	public LInteger bnot() {
		return LInteger.valueOf(~this.exactLongValue());
	}

	public LInteger shl(LNumber that) {
		return LInteger.valueOf(this.exactLongValue() << that.exactLongValue());
	}

	public LInteger shr(LNumber that) {
		return LInteger.valueOf(this.exactLongValue() >>> that.exactLongValue());
	}

	public abstract boolean eq(LNumber that);

	public abstract boolean lt(LNumber that);

	public abstract boolean le(LNumber that);

}
