package net.sandius.rembulan.core;

public abstract class ComparisonImplementation {

	public static final IntegerComparisonImplementation INTEGER_CMP = new IntegerComparisonImplementation();
	public static final FloatComparisonImplementation FLOAT_CMP = new FloatComparisonImplementation();
	public static final StringComparisonImplementation STRING_CMP = new StringComparisonImplementation();

	public abstract boolean do_eq(Object a, Object b);
	public abstract boolean do_lt(Object a, Object b);
	public abstract boolean do_le(Object a, Object b);

	public static class IntegerComparisonImplementation extends ComparisonImplementation {

		@Override
		public boolean do_eq(Object a, Object b) {
			return ((Number) a).longValue() == ((Number) b).longValue();
		}

		@Override
		public boolean do_lt(Object a, Object b) {
			return ((Number) a).longValue() < ((Number) b).longValue();
		}

		@Override
		public boolean do_le(Object a, Object b) {
			return ((Number) a).longValue() <= ((Number) b).longValue();
		}

	}

	public static class FloatComparisonImplementation extends ComparisonImplementation {

		@Override
		public boolean do_eq(Object a, Object b) {
			return ((Number) a).doubleValue() == ((Number) b).doubleValue();
		}

		@Override
		public boolean do_lt(Object a, Object b) {
			return ((Number) a).doubleValue() < ((Number) b).doubleValue();
		}

		@Override
		public boolean do_le(Object a, Object b) {
			return ((Number) a).doubleValue() <= ((Number) b).doubleValue();
		}

	}

	public static class StringComparisonImplementation extends ComparisonImplementation {

		@Override
		public boolean do_eq(Object a, Object b) {
			return ((String) a).compareTo((String) b) == 0;
		}

		@Override
		public boolean do_lt(Object a, Object b) {
			return ((String) a).compareTo((String) b) < 0;
		}

		@Override
		public boolean do_le(Object a, Object b) {
			return ((String) a).compareTo((String) b) <= 0;
		}

	}

	public static ComparisonImplementation of(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			return of((Number) a, (Number) b);
		}
		else if (a instanceof String && b instanceof String) {
			return STRING_CMP;
		}
		else {
			return null;
		}
	}

	public static ComparisonImplementation of(Number a, Number b) {
		if (a == null || b == null) {
			return null;
		}
		if ((a instanceof Double || a instanceof Float)
				|| (b instanceof Double || b instanceof Float)) {
			return FLOAT_CMP;
		}
		else {
			return INTEGER_CMP;
		}
	}

}
