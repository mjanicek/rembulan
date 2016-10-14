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

package net.sandius.rembulan;

import net.sandius.rembulan.runtime.Coroutine;
import net.sandius.rembulan.runtime.LuaFunction;

/**
 * An enum representing a Lua type.
 *
 * <p>There are eight types in Lua ({@code nil}, {@code boolean}, {@code number}, {@code string},
 * {@code function}, {@code userdata}, {@code thread} and {@code table}). In Rembulan,
 * all Java object references are mapped to a Lua type according to the following list:</p>
 *
 * <ul>
 *     <li>{@link #NIL} ... no explicit {@code nil} type; any (Java) {@code null} value is
 *                          considered <b>nil</b></li>
 *     <li>{@link #BOOLEAN} ... instances of {@link Boolean java.lang.Boolean}</li>
 *     <li>{@link #NUMBER} ... instances of {@link Number java.lang.Number}:
 *       <ul>
 *         <li><i>float</i> ... {@link Double java.lang.Double} (canonical)
 *                              or {@link Float java.lang.Float}</li>
 *         <li><i>integer</i> ... any other subclass of {@code java.lang.Number},
 *                                with {@link Long java.lang.Long} being the canonical
 *                                representation</li>
 *       </ul>
 *     </li>
 *     <li>{@link #STRING} ... instances of {@link String java.lang.String}</li>
 *     <li>{@link #FUNCTION} ... {@link LuaFunction}</li>
 *     <li>{@link #USERDATA}:
 *       <ul>
 *           <li><i>full userdata</i> ... {@link Userdata}</li>
 *           <li><i>light userdata</i> ... instances of any class other than those mentioned
 *                                         in this list</li>
 *       </ul>
 *     </li>
 *     <li>{@link #THREAD} ... {@link Coroutine}</li>
 *     <li>{@link #TABLE} ... {@link Table}</li>
 * </ul>
 *
 * <p>For numeric values, the <i>canonical representation</i> is the default, full-precision
 * representation of the number as either a float or integer. To convert a number to its
 * canonical value, use {@link Conversions#toCanonicalNumber(Number)}.</p>
 *
 * <p>To retrieve the name of the type of a Lua value, use the {@link ValueTypeNamer}
 * interface (for names based on the type only, without taking into account the {@code __name}
 * metamethod, use {@link PlainValueTypeNamer}).</p>
 */
public enum LuaType {

	/**
	 * The Lua {@code nil} type, corresponding to {@code null} references.
	 */
	NIL,

	/**
	 * The Lua {@code boolean} type, corresponding to instances
	 * of {@link Boolean java.lang.Boolean}.
	 */
	BOOLEAN,

	/**
	 * The Lua {@code number} type, corresponding to instances of {@link Number java.lang.Number}.
	 *
	 * <p>Instances of {@link Double java.lang.Double} and {@link Float java.lang.Float}
	 * are mapped to Lua floats ({@code Double}s being the canonical representation). All other
	 * subclasses of {@code Number} are mapped to Lua integers, with {@link Long java.lang.Long}
	 * being the canonical representation.</p>
	 */
	NUMBER,

	/**
	 * The Lua {@code string} type, corresponding to instances of {@link ByteString}
	 * and {@link String java.lang.String}.
	 */
	STRING,

	/**
	 * The Lua {@code function} type, corresponding to instances of {@link LuaFunction}.
	 */
	FUNCTION,

	/**
	 * The Lua {@code userdata} type, corresponding to instances of {@link Userdata} (for full
	 * userdata), or any other subclasses of {@link java.lang.Object} not mapped to a Lua
	 * type (for light userdata).
	 */
	USERDATA,

	/**
	 * The Lua {@code thread} type, corresponding to instances of the {@link Coroutine} class.
	 */
	THREAD,

	/**
	 * The Lua {@code table} type, corresponding to instances of {@link Table}.
	 */
	TABLE;

	/**
	 * Returns the Lua type of the object {@code o}.
	 *
	 * @param o  the object to determine the type of, may be {@code null}
	 * @return  the Lua type of {@code o}
	 */
	public static LuaType typeOf(Object o) {
		if (o == null) return LuaType.NIL;
		else if (o instanceof Boolean) return LuaType.BOOLEAN;
		else if (o instanceof Number) return LuaType.NUMBER;
		else if (o instanceof ByteString || o instanceof String) return LuaType.STRING;
		else if (o instanceof Table) return LuaType.TABLE;
		else if (o instanceof LuaFunction) return LuaType.FUNCTION;
		else if (o instanceof Coroutine) return LuaType.THREAD;
		else return LuaType.USERDATA;
	}

	/**
	 * Returns {@code true} iff {@code o} is <b>nil</b>.
	 *
	 * <p>{@code o} is <b>nil</b> if and only if {@code o} is {@code null}.</p>
	 *
	 * @param o  the object to test for being <b>nil</b>, may be {@code null}
	 * @return  {@code true} iff {@code o} is <b>nil</b>
	 */
	public static boolean isNil(Object o) {
		return o == null;
	}

	/**
	 * Returns {@code true} iff {@code o} is a Lua boolean.
	 *
	 * <p>{@code o} is a Lua boolean if and only if {@code o} is an instance of
	 * {@link Boolean java.lang.Boolean}.</p>
	 *
	 * @param o  the object to test for being a boolean, may be {@code null}
	 * @return  {@code true} iff {@code o} is a Lua boolean
	 */
	public static boolean isBoolean(Object o) {
		return o instanceof Boolean;
	}

	/**
	 * Returns {@code true} iff {@code o} is a Lua number.
	 *
	 * <p>{@code o} is a Lua number if and only if {@code o} is an instance of
	 * {@link Number java.lang.Number}.</p>
	 *
	 * @param o  the object to test for being a number, may be {@code null}
	 * @return  {@code true} iff {@code o} is a Lua number
	 */
	public static boolean isNumber(Object o) {
		return o instanceof Number;
	}

	/**
	 * Returns {@code true} iff {@code o} is a Lua float.
	 *
	 * <p>{@code o} is a Lua float if and only if {@code o} is an instance of
	 * {@link Double java.lang.Double} or {@link Float java.lang.Float}.</p>
	 *
	 * @param o  the object to test for being a float, may be {@code null}
	 * @return  {@code true} iff {@code o} is a Lua float
	 */
	public static boolean isFloat(Object o) {
		return o instanceof Double || o instanceof Float;
	}

	/**
	 * Returns {@code true} iff {@code o} is a Lua integer.
	 *
	 * <p>{@code o} is a Lua number if and only if {@code o} is a Lua number and is not
	 * a Lua float.</p>
	 *
	 * @param o  the object to test for being an integer, may be {@code null}
	 * @return  {@code true} iff {@code o} is a Lua integer
	 */
	public static boolean isInteger(Object o) {
		return isNumber(o) && !isFloat(o);
	}

	/**
	 * Returns {@code true} iff {@code o} is a Lua string.
	 *
	 * <p>{@code o} is a Lua string if and only if {@code o} is an instance of
	 * {@link ByteString} or {@link String java.lang.String}.</p>
	 *
	 * @param o  the object to test for being a string, may be {@code null}
	 * @return  {@code true} iff {@code o} is a Lua string
	 */
	public static boolean isString(Object o) {
		return o instanceof ByteString || o instanceof String;
	}

	/**
	 * Returns {@code true} iff {@code o} is a Lua function.
	 *
	 * <p>{@code o} is a Lua function if and only if {@code o} is an instance of
	 * {@link LuaFunction}.</p>
	 *
	 * @param o  the object to test for being a function, may be {@code null}
	 * @return  {@code true} iff {@code o} is a Lua function
	 */
	public static boolean isFunction(Object o) {
		return o instanceof LuaFunction;
	}

	/**
	 * Returns {@code true} iff {@code o} is a Lua userdata.
	 *
	 * <p>{@code o} is a Lua userdata if it is not {@code nil}, {@code boolean}, {@code number},
	 * {@code string}, {@code function}, {@code thread} or {@code table}.</p>
	 *
	 * @param o  the object to test for being a userdata, may be {@code null}
	 * @return  {@code true} iff {@code o} is a Lua userdata
	 */
	public static boolean isUserdata(Object o) {
		return typeOf(o) == USERDATA;
	}

	/**
	 * Returns {@code true} iff {@code o} is full userdata.
	 *
	 * <p>{@code o} is full userdata if and only if {@code o} is an instance of
	 * {@link Userdata}.</p>
	 *
	 * @param o  the object to test for being full userdata, may be {@code null}
	 * @return  {@code true} iff {@code o} is full userdata
	 */
	public static boolean isFullUserdata(Object o) {
		return o instanceof Userdata;
	}

	/**
	 * Returns {@code true} iff the object {@code o} is light userdata.
	 *
	 * <p>An object is light userdata when its Lua type is {@link #USERDATA} and it
	 * is not an instance of the {@link Userdata} class. In other words, it is not an
	 * instance of any class mapped to a Lua type.</p>
	 *
	 * @param o  the object to test for being light userdata, may be {@code null}
	 * @return  {@code true} iff {@code o} is light userdata
	 */
	public static boolean isLightUserdata(Object o) {
		return !isFullUserdata(o) && isUserdata(o);
	}

	/**
	 * Returns {@code true} iff the object {@code o} is a Lua thread.
	 *
	 * <p>{@code o} is a Lua thread if and only if {@code o} is an instance of {@link Coroutine}.</p>
	 *
	 * @param o  the object to test for being a Lua thread, may be {@code null}
	 * @return  {@code true} iff {@code o} is a Lua thread
	 */
	public static boolean isThread(Object o) {
		return o instanceof Coroutine;
	}

	/**
	 * Returns {@code true} iff the object {@code o} is a Lua table.
	 *
	 * <p>{@code o} is a Lua table if and only if {@code o} is an instance of {@link Table}.</p>
	 *
	 * @param o  the object to test for being a Lua table, may be {@code null}
	 * @return  {@code true} iff {@code o} is a Lua table
	 */
	public static boolean isTable(Object o) {
		return o instanceof Table;
	}

}
