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

package net.sandius.rembulan.core.impl;

@Deprecated
public abstract class Varargs {

	private Varargs() {
		// not to be instantiated or extended
	}

	@Deprecated
	public static Object getElement(Object[] a, int idx) {
		return idx >= 0 && idx < a.length ? a[idx] : null;
	}

	@Deprecated
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
