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

final class JavaConversions {

	private JavaConversions() {
		// not to be instantiated
	}

	public static Object toLuaValue(Object o) {
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

	public static Long toLong(Long l) {
		return l;
	}

	public static Integer toInteger(Long l) {
		long ll = l.longValue();
		int i = (int) ll;
		return (long) i == ll ? Integer.valueOf(i) : null;
	}

	public static Short toShort(Long l) {
		long ll = l.longValue();
		short s = (short) ll;
		return (long) s == ll ? Short.valueOf(s) : null;
	}

	public static Byte toByte(Long l) {
		long ll = l.longValue();
		byte b = (byte) ll;
		return (long) b == ll ? Byte.valueOf(b) : null;
	}

	public static Character toChar(Long l) {
		long ll = l.longValue();
		char c = (char) ll;
		return (long) c == ll ? Character.valueOf(c) : null;
	}

	public static Double toDouble(Double d) {
		return d;
	}

	public static Float toFloat(Double d) {
		return Float.valueOf(d.floatValue());
	}

	public static Long toLong(Number n) {
		return Conversions.integerValueOf(n);
	}

	public static Integer toInteger(Number n) {
		Long l = Conversions.integerValueOf(n);
		return l != null ? toInteger(l) : null;
	}

	public static Short toShort(Number n) {
		Long l = Conversions.integerValueOf(n);
		return l != null ? toShort(l) : null;
	}

	public static Byte toByte(Number n) {
		Long l = Conversions.integerValueOf(n);
		return l != null ? toByte(l) : null;
	}

	public static Character toChar(Number n) {
		Long l = Conversions.integerValueOf(n);
		return l != null ? toChar(l) : null;
	}

	public static Double toDouble(Number n) {
		return Conversions.floatValueOf(n);
	}

	public static Float toFloat(Number n) {
		return toFloat(Conversions.floatValueOf(n));
	}

	public static Long toLong(Object o) {
		if (o instanceof Long) return toLong((Long) o);
		else if (o instanceof Number) return toLong((Number) o);
		else {
			Number n = Conversions.numericalValueOf(o);
			return n != null ? toLong(n) : null;
		}
	}

	public static Integer toInteger(Object o) {
		if (o instanceof Long) return toInteger((Long) o);
		else if (o instanceof Number) return toInteger((Number) o);
		else {
			Number n = Conversions.numericalValueOf(o);
			return n != null ? toInteger(n) : null;
		}
	}

	public static Short toShort(Object o) {
		if (o instanceof Long) return toShort((Long) o);
		else if (o instanceof Number) return toShort((Number) o);
		else {
			Number n = Conversions.numericalValueOf(o);
			return n != null ? toShort(n) : null;
		}
	}

	public static Byte toByte(Object o) {
		if (o instanceof Long) return toByte((Long) o);
		else if (o instanceof Number) return toByte((Number) o);
		else {
			Number n = Conversions.numericalValueOf(o);
			return n != null ? toByte(n) : null;
		}
	}

	public static Character toChar(Object o) {
		if (o instanceof Long) return toChar((Long) o);
		else if (o instanceof Number) return toChar((Number) o);
		else {
			Number n = Conversions.numericalValueOf(o);
			return n != null ? toChar(n) : null;
		}
	}

	public static Double toDouble(Object o) {
		if (o instanceof Double) return toDouble((Double) o);
		else if (o instanceof Number) return toDouble((Number) o);
		else {
			Number n = Conversions.numericalValueOf(o);
			return n != null ? toDouble(n) : null;
		}
	}

	public static Float toFloat(Object o) {
		if (o instanceof Double) return toFloat((Double) o);
		else if (o instanceof Number) return toFloat((Number) o);
		else {
			Number n = Conversions.numericalValueOf(o);
			return n != null ? toFloat(n) : null;
		}
	}

	public static Boolean toBoolean(Object o) {
		return Conversions.booleanValueOf(o);
	}

}
