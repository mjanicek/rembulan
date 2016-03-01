package net.sandius.rembulan.core;

public abstract class MathImplementation {

	public static final MathImplementation INTEGER_MATH = new IntegerMathImplementation();
	public static final MathImplementation FLOAT_MATH = new FloatMathImplementation();

	public abstract Number do_add(Number a, Number b);

	public static class IntegerMathImplementation extends MathImplementation {
		@Override
		public Number do_add(Number a, Number b) {
			return a.longValue() + b.longValue();
		}
	}

	public static class FloatMathImplementation extends MathImplementation {
		@Override
		public Number do_add(Number a, Number b) {
			return a.doubleValue() + b.doubleValue();
		}
	}

	public static MathImplementation math_add(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			if ((a instanceof Double || a instanceof Float)
					|| (b instanceof Double || b instanceof Float)) {
				return FLOAT_MATH;
			}
			else {
				return INTEGER_MATH;
			}
		}
		else if (a instanceof String || b instanceof String) {
			return math_add(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b));
		}
		else {
			return null;
		}
	}

}
