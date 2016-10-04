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

package net.sandius.rembulan.lib.luajava;

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.LuaObject;
import net.sandius.rembulan.runtime.Coroutine;
import net.sandius.rembulan.runtime.LuaFunction;

final class Unmapper {

	private Unmapper() {
		// not to be instantiated
	}

	public static Object unmapFrom(Object o) {
		if (o instanceof Class) {
			return ClassWrapper.of((Class<?>) o);
		}
		else {
			if (o == null || o instanceof Boolean || o instanceof String) {
				return o;
			}
			else if (o instanceof Number) {
				return Conversions.toCanonicalNumber((Number) o);
			}
			else if (o instanceof Character) {
				return Long.valueOf(((Character) o).charValue());
			}
			else if (o instanceof LuaFunction || o instanceof Coroutine || o instanceof LuaObject) {
				return o;
			}
			else return ObjectWrapper.of(o);
		}
	}

}
