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

package net.sandius.rembulan.parser.util;

import java.util.Iterator;
import java.util.List;

public abstract class Util {

	private Util() {
		// not to be instantiated
	}

	public static String iterableToString(Iterable<?> collection, String sep) {
		StringBuilder bld = new StringBuilder();
		Iterator<?> it = collection.iterator();
		while (it.hasNext()) {
			bld.append(it.next());
			if (it.hasNext()) {
				bld.append(sep);
			}
		}
		return bld.toString();
	}

	public static String listToString(List<?> l, String sep) {
		return iterableToString(l, sep);
	}

}
