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

public class StringPattern {

	private StringPattern() {
	}

	public static StringPattern fromString(String pattern, boolean ignoreCaret) {
		throw new UnsupportedOperationException();  // TODO
	}

	public static StringPattern fromString(String pattern) {
		return fromString(pattern, false);
	}

	public interface MatchAction {
		void onMatch(String s, int firstIndex, int lastIndex);
		// if value == null, it's just the index
		void onCapture(String s, int index, String value);
	}

	// returns the index immediately following the match,
	// or 0 if not match was found
	public int match(String s, int fromIndex, MatchAction action) {
		throw new UnsupportedOperationException();  // TODO
	}

}
