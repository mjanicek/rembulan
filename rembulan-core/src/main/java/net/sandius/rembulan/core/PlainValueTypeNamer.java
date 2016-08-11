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

import static net.sandius.rembulan.LuaFormat.TYPENAME_BOOLEAN;
import static net.sandius.rembulan.LuaFormat.TYPENAME_FUNCTION;
import static net.sandius.rembulan.LuaFormat.TYPENAME_NIL;
import static net.sandius.rembulan.LuaFormat.TYPENAME_NUMBER;
import static net.sandius.rembulan.LuaFormat.TYPENAME_STRING;
import static net.sandius.rembulan.LuaFormat.TYPENAME_TABLE;
import static net.sandius.rembulan.LuaFormat.TYPENAME_THREAD;
import static net.sandius.rembulan.LuaFormat.TYPENAME_USERDATA;

public class PlainValueTypeNamer implements ValueTypeNamer {

	public static final PlainValueTypeNamer INSTANCE = new PlainValueTypeNamer();

	public static String luaTypeToName(LuaType type) {
		switch (type) {
			case NIL: return TYPENAME_NIL;
			case BOOLEAN: return TYPENAME_BOOLEAN;
			case NUMBER: return TYPENAME_NUMBER;
			case STRING: return TYPENAME_STRING;
			case TABLE: return TYPENAME_TABLE;
			case FUNCTION: return TYPENAME_FUNCTION;
			case USERDATA: return TYPENAME_USERDATA;
			case THREAD: return TYPENAME_THREAD;
			default: throw new IllegalStateException("Illegal type: " + type);
		}
	}

	@Override
	public String typeNameOf(Object instance) {
		return luaTypeToName(Values.typeOf(instance));
	}

}
