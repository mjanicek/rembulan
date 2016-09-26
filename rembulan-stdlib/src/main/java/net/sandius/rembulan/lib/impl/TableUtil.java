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

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.Metatables;
import net.sandius.rembulan.Ordering;
import net.sandius.rembulan.Table;

final class TableUtil {

	private TableUtil() {
		// not to be instantiated
	}

	public static boolean hasLenMetamethod(Table t) {
		return Metatables.getMetamethod(Metatables.MT_LEN, t) != null;
	}

	public static boolean hasIndexMetamethod(Table t) {
		return Metatables.getMetamethod(Metatables.MT_INDEX, t) != null;
	}

	public static boolean hasNewIndexMetamethod(Table t) {
		return Metatables.getMetamethod(Metatables.MT_NEWINDEX, t) != null;
	}

	public static Ordering<Object> rawSequenceOrderingOf(Table t, long firstIdx, long lastIdx) {
		long count = lastIdx - firstIdx + 1;

		if (count < 2) {
			throw new IllegalArgumentException("Interval is empty: [" + firstIdx + ", " + lastIdx + "]");
		}

		// every value between [firstIdx..lastIdx] is retrieved at most once

		final Ordering<Object> result;
		{
			// the first pair determines the ordering
			Object first = t.rawget(firstIdx);
			result = Ordering.of(first, t.rawget(firstIdx + 1));

			if (count % 2 == 1) {
				// odd number of values: compare first with last
				if (result != Ordering.of(first, t.rawget(lastIdx))) {
					return null;
				}
			}
		}

		// check the remaining value pairs
		for (long idx = firstIdx + 2; idx + 1 <= lastIdx; idx += 2) {
			if (result != Ordering.of(t.rawget(idx), t.rawget(idx + 1))) {
				return null;
			}
		}

		return result;
	}

}
