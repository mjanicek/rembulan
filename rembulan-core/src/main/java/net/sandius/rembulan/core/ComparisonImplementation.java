package net.sandius.rembulan.core;

public abstract class ComparisonImplementation {

	public static final NumericComparisonImplementation NUMERIC_CMP = new NumericComparisonImplementation();
	public static final StringComparisonImplementation STRING_CMP = new StringComparisonImplementation();

	public abstract boolean do_eq(Object a, Object b);
	public abstract boolean do_lt(Object a, Object b);
	public abstract boolean do_le(Object a, Object b);

	public static class NumericComparisonImplementation extends ComparisonImplementation {

		@Override
		public boolean do_eq(Object a, Object b) {
			return LNumber.eq((LNumber) a, (LNumber) b);
		}

		@Override
		public boolean do_lt(Object a, Object b) {
			return LNumber.lt((LNumber) a, (LNumber) b);
		}

		@Override
		public boolean do_le(Object a, Object b) {
			return LNumber.le((LNumber) a, (LNumber) b);
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
		if (a instanceof LNumber && b instanceof LNumber) {
			return NUMERIC_CMP;
		}
		else if (a instanceof String && b instanceof String) {
			return STRING_CMP;
		}
		else {
			return null;
		}
	}

}
