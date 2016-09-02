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

import net.sandius.rembulan.LuaType;

public abstract class Values {

	private Values() {
		// not to be instantiated or extended
	}

	/*
	 * Mappings between types:
	 *
	 * Lua       |  Java (Rembulan)
	 * ----------+---------------------
	 * nil       |  null pointer
	 * boolean   |  java.lang.Boolean
	 * number    |  java.lang.Number;
	 *           |  floats: java.lang.Double (canonical), java.lang.Float
	 *           |  integers: any other subclass of Number, java.lang.Long being
	 *           |            the canonical representation
	 * string    |  java.lang.String
	 * table     |  net.sandius.rembulan.core.Table
	 * function  |  net.sandius.rembulan.core.Function
	 * userdata  |  full userdata: net.sandius.rembulan.core.Userdata
	 *           |  light userdata: any class other than those mentioned here
	 */
	public static LuaType typeOf(Object v) {
		if (v == null) return LuaType.NIL;
		else if (v instanceof Boolean) return LuaType.BOOLEAN;
		else if (v instanceof Number) return LuaType.NUMBER;
		else if (v instanceof String) return LuaType.STRING;
		else if (v instanceof Table) return LuaType.TABLE;
		else if (v instanceof Invokable) return LuaType.FUNCTION;
		else if (v instanceof Coroutine) return LuaType.THREAD;
		else return LuaType.USERDATA;
	}

	public static boolean isNaN(Object o) {
		return (o instanceof Double || o instanceof Float)
				&& Double.isNaN(((Number) o).doubleValue());
	}

	public static boolean isLightUserdata(Object o) {
		return typeOf(o) == LuaType.USERDATA && !(o instanceof Userdata);
	}

}
