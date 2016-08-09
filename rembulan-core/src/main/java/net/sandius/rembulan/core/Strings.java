/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
