package net.sandius.rembulan.util;

public enum PartialOrderComparisonResult {

	EQUAL,
	LESSER_THAN,
	GREATER_THAN,
	NOT_COMPARABLE;

	public boolean isDefined() {
		return this != NOT_COMPARABLE;
	}

	public static PartialOrderComparisonResult fromTotalOrderComparison(int cmp) {
		if (cmp < 0) return LESSER_THAN;
		else if (cmp > 0) return GREATER_THAN;
		else return EQUAL;
	}

	public int toTotalOrderComparison() {
		switch (this) {
			case EQUAL: return 0;
			case LESSER_THAN: return -1;
			case GREATER_THAN: return +1;
			default: throw new IllegalArgumentException("Not comparable");
		}
	}

}
