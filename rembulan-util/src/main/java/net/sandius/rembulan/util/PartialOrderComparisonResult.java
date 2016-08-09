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
