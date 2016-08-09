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

package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.analysis.types.LuaTypes;
import net.sandius.rembulan.compiler.analysis.types.Type;

public enum NumericOperationType {

	Integer,
	Float,
	Number,
	Any;

	public Type toType() {
		switch (this) {
			case Integer:  return LuaTypes.NUMBER_INTEGER;
			case Float:    return LuaTypes.NUMBER_FLOAT;
			case Number:   return LuaTypes.NUMBER;
			case Any:
			default:       return LuaTypes.ANY;
		}
	}

}
