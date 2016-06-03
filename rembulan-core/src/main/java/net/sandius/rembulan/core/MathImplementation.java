package net.sandius.rembulan.core;

import static net.sandius.rembulan.core.RawOperators.*;

public abstract class MathImplementation {

	public static final IntegerMathImplementation INTEGER_MATH = new IntegerMathImplementation();
	public static final FloatMathImplementation FLOAT_MATH = new FloatMathImplementation();

	public abstract Number do_add(Number a, Number b);
	public abstract Number do_sub(Number a, Number b);
	public abstract Number do_mul(Number a, Number b);
	public abstract Double do_div(Number a, Number b);
	public abstract Number do_mod(Number a, Number b);
	public abstract Number do_idiv(Number a, Number b);
	public abstract Double do_pow(Number a, Number b);

	public abstract Number do_unm(Number n);

	public abstract boolean do_eq(Number a, Number b);
	public abstract boolean do_lt(Number a, Number b);
	public abstract boolean do_le(Number a, Number b);

	public static class IntegerMathImplementation extends MathImplementation {

		@Override
		public Long do_add(Number a, Number b) {
			return a.longValue() + b.longValue();
		}

		@Override
		public Long do_sub(Number a, Number b) {
			return a.longValue() - b.longValue();
		}

		@Override
		public Long do_mul(Number a, Number b) {
			return a.longValue() * b.longValue();
		}

		@Override
		public Double do_div(Number a, Number b) {
			return a.doubleValue() / b.doubleValue();
		}

		@Override
		public Long do_mod(Number a, Number b) {
			return rawmod(a.longValue(), b.longValue());
		}

		@Override
		public Long do_idiv(Number a, Number b) {
			return rawidiv(a.longValue(), b.longValue());
		}

		@Override
		public Double do_pow(Number a, Number b) {
			return rawpow(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Number do_unm(Number n) {
			return -n.longValue();
		}

		@Override
		public boolean do_eq(Number a, Number b) {
			return a.longValue() == b.longValue();
		}

		@Override
		public boolean do_lt(Number a, Number b) {
			return a.longValue() < b.longValue();
		}

		@Override
		public boolean do_le(Number a, Number b) {
			return a.longValue() <= b.longValue();
		}

	}

	public static class FloatMathImplementation extends MathImplementation {

		@Override
		public Double do_add(Number a, Number b) {
			return a.doubleValue() + b.doubleValue();
		}

		@Override
		public Double do_sub(Number a, Number b) {
			return a.doubleValue() - b.doubleValue();
		}

		@Override
		public Number do_mul(Number a, Number b) {
			return a.doubleValue() * b.doubleValue();
		}

		@Override
		public Double do_div(Number a, Number b) {
			return a.doubleValue() / b.doubleValue();
		}

		@Override
		public Double do_mod(Number a, Number b) {
			return rawmod(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_idiv(Number a, Number b) {
			return rawidiv(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_pow(Number a, Number b) {
			return rawpow(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Number do_unm(Number n) {
			return -n.doubleValue();
		}

		@Override
		public boolean do_eq(Number a, Number b) {
			return a.doubleValue() == b.doubleValue();
		}

		@Override
		public boolean do_lt(Number a, Number b) {
			return a.doubleValue() < b.doubleValue();
		}

		@Override
		public boolean do_le(Number a, Number b) {
			return a.doubleValue() <= b.doubleValue();
		}

	}

	@Deprecated
	public static MathImplementation arithmetic(Object a, Object b) {
		return arithmetic(Conversions.arithmeticValueOf(a), Conversions.arithmeticValueOf(b));
	}

	public static MathImplementation arithmetic(Number a, Number b) {
		if (a == null || b == null) {
			return null;
		}
		else if ((a instanceof Double || a instanceof Float)
				|| (b instanceof Double || b instanceof Float)) {
			return FLOAT_MATH;
		}
		else {
			return INTEGER_MATH;
		}
	}

	@Deprecated
	public static MathImplementation arithmetic(Object o) {
		return arithmetic(Conversions.arithmeticValueOf(o));
	}

	public static MathImplementation arithmetic(Number n) {
		if (n == null) {
			return null;
		}
		else if (n instanceof Double || n instanceof Float) {
			return FLOAT_MATH;
		}
		else {
			return INTEGER_MATH;
		}
	}

}
