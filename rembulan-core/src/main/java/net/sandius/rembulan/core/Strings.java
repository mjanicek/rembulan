package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public abstract class Strings {

	private Strings() {
		// not to be instantiated
	}

	public Long stringToNumber(String s, int base) {
		Check.notNull(s);

		if (base < 2 || base > 36) {
			throw new IllegalArgumentException("base must be in the range [2..36], got " + base);
		}

		String str = s.trim();

		int maxDigit = Math.min(base, 9);
		int maxLetter = Math.max(base - 10, 0);

		if (str.length() > 0) {
			boolean positive = true;
			int idx = 0;

			if (str.charAt(0) == '-') {
				positive = false;
				idx = 1;
			}
			else if (str.charAt(0) == '+') {
				idx = 1;
			}

			long n = 0;

			for (int i = idx; i < str.length(); i++) {
				char c = str.charAt(i);

				int x;
				if (c >= '0' && c < '0' + maxDigit) {
					x = c - '0';
				}
				else if (c >= 'A' && c < 'A' + maxLetter) {
					x = c - 'A' + 10;
				}
				else {
					return null;
				}

				n = (n * base) + x;
			}

			return positive ? n : -n;
		}
		else {
			return null;
		}
	}

}
