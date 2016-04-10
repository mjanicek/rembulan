package net.sandius.rembulan.core.impl;

public abstract class Varargs {

	private Varargs() {
		// not to be instantiated or extended
	}

	public static Object getElement(Object[] a, int idx) {
		return idx >= 0 && idx < a.length ? a[idx] : null;
	}

	public static Object[] from(Object[] args, int fromIndex) {
		assert (fromIndex >= 0);

		int n = args.length - fromIndex;

		if (n > 0) {
			Object[] result = new Object[n];
			System.arraycopy(args, fromIndex, result, 0, n);
			return result;
		}
		else {
			return new Object[0];
		}
	}

}
