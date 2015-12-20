package net.sandius.rembulan.util.testing;

public class Assertions {

	public static void assertEquals(Object actual, Object expected) {
		if (actual == null) {
			if (expected != null) {
				throw new AssertionError("Expected " + expected + ", got null");
			}
		}
		else {
			if (!actual.equals(expected)) {
				throw new AssertionError("Expected " + expected + ", got " + actual);
			}
		}
	}

}
