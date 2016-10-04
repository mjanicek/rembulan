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
import net.sandius.rembulan.LuaMathOperators;

class DistanceMappingVisitor implements ParameterMappingVisitor<Integer> {

	static final DistanceMappingVisitor INSTANCE = new DistanceMappingVisitor();

	private static final int INTEGER_TO_LONG   = 0;
	private static final int INTEGER_TO_INT    = 1;
	private static final int INTEGER_TO_SHORT  = 2;
	private static final int INTEGER_TO_BYTE   = 3;
	private static final int INTEGER_TO_CHAR   = 4;
	private static final int INTEGER_TO_DOUBLE = 5;
	private static final int INTEGER_TO_FLOAT  = 6;

	private static final int FLOAT_TO_LONG   = 2;
	private static final int FLOAT_TO_INT    = 3;
	private static final int FLOAT_TO_SHORT  = 4;
	private static final int FLOAT_TO_BYTE   = 5;
	private static final int FLOAT_TO_CHAR   = 6;
	private static final int FLOAT_TO_DOUBLE = 0;
	private static final int FLOAT_TO_FLOAT  = 1;

	private static final int STRING_TO_NUMBER = 7;

	private static Integer add(Integer a, Integer b) {
		if (a != null && b != null) {
			return a + b;
		}
		else {
			return null;
		}
	}

	@Override
	public Integer visitReferenceParameter(Class<?> clazz, Object arg) {
		return arg instanceof JavaWrapper
				? referenceParam(clazz, ((JavaWrapper) arg).get())
				: referenceParam(clazz, arg);
	}

	private static Integer referenceParam(Class<?> clazz, Object arg) {
		return clazz.isAssignableFrom(arg.getClass()) ? Integer.valueOf(0) : null;
	}

	@Override
	public Integer visitLongParameter(Object arg) {
		if (arg instanceof Number) {
			return longParam(Conversions.toCanonicalNumber((Number) arg));
		}
		else if (arg instanceof String) {
			Number n = Conversions.numericalValueOf((String) arg);
			return n != null ? add(STRING_TO_NUMBER, longParam(n)) : null;
		}
		else {
			return null;
		}
	}

	@Override
	public Integer visitIntParameter(Object arg) {
		if (arg instanceof Number) {
			return intParam(Conversions.toCanonicalNumber((Number) arg));
		}
		else if (arg instanceof String) {
			Number n = Conversions.numericalValueOf((String) arg);
			return n != null ? add(STRING_TO_NUMBER, intParam(n)) : null;
		}
		else {
			return null;
		}
	}

	@Override
	public Integer visitShortParameter(Object arg) {
		if (arg instanceof Number) {
			return shortParam(Conversions.toCanonicalNumber((Number) arg));
		}
		else if (arg instanceof String) {
			Number n = Conversions.numericalValueOf((String) arg);
			return n != null ? add(STRING_TO_NUMBER, shortParam(n)) : null;
		}
		else {
			return null;
		}
	}

	@Override
	public Integer visitByteParameter(Object arg) {
		if (arg instanceof Number) {
			return byteParam(Conversions.toCanonicalNumber((Number) arg));
		}
		else if (arg instanceof String) {
			Number n = Conversions.numericalValueOf((String) arg);
			return n != null ? add(STRING_TO_NUMBER, byteParam(n)) : null;
		}
		else {
			return null;
		}
	}

	@Override
	public Integer visitCharParameter(Object arg) {
		if (arg instanceof Number) {
			return charParam(Conversions.toCanonicalNumber((Number) arg));
		}
		else if (arg instanceof String) {
			Number n = Conversions.numericalValueOf((String) arg);
			return n != null ? add(STRING_TO_NUMBER, charParam(n)) : null;
		}
		else {
			return null;
		}
	}

	@Override
	public Integer visitDoubleParameter(Object arg) {
		if (arg instanceof Number) {
			return doubleParam(Conversions.toCanonicalNumber((Number) arg));
		}
		else if (arg instanceof String) {
			Number n = Conversions.numericalValueOf((String) arg);
			return n != null ? add(STRING_TO_NUMBER, doubleParam(n)) : null;
		}
		else {
			return null;
		}
	}

	@Override
	public Integer visitFloatParameter(Object arg) {
		if (arg instanceof Number) {
			return floatParam(Conversions.toCanonicalNumber((Number) arg));
		}
		else if (arg instanceof String) {
			Number n = Conversions.numericalValueOf((String) arg);
			return n != null ? add(STRING_TO_NUMBER, floatParam(n)) : null;
		}
		else {
			return null;
		}
	}

	private static Integer integralParam(Number n, long min, long max, int integerDistance, int floatDistance) {
		if (n instanceof Long) {
			long l = (Long) n;
			return l >= min && l <= max ? integerDistance : null;
		}
		else if (n instanceof Double
				&& LuaMathOperators.hasExactIntegerRepresentation(n.doubleValue())) {
			long l = (long) n.doubleValue();
			return l >= min && l <= max ? floatDistance : null;
		}
		else {
			return null;
		}
	}

	private static Integer longParam(Number n) {
		return integralParam(n, Long.MIN_VALUE, Long.MAX_VALUE, INTEGER_TO_LONG, FLOAT_TO_LONG);
	}

	private static Integer intParam(Number n) {
		return integralParam(n, Integer.MIN_VALUE, Integer.MAX_VALUE, INTEGER_TO_INT, FLOAT_TO_INT);
	}

	private static Integer shortParam(Number n) {
		return integralParam(n, Short.MIN_VALUE, Short.MAX_VALUE, INTEGER_TO_SHORT, FLOAT_TO_SHORT);
	}

	private static Integer byteParam(Number n) {
		return integralParam(n, Byte.MIN_VALUE, Byte.MAX_VALUE, INTEGER_TO_BYTE, FLOAT_TO_BYTE);
	}

	private static Integer charParam(Number n) {
		return integralParam(n, Character.MIN_CODE_POINT, Character.MAX_CODE_POINT, INTEGER_TO_CHAR, FLOAT_TO_CHAR);
	}

	private static Integer doubleParam(Number n) {
		return n instanceof Long ? INTEGER_TO_DOUBLE : FLOAT_TO_DOUBLE;
	}

	private static Integer floatParam(Number n) {
		return n instanceof Long ? INTEGER_TO_FLOAT : FLOAT_TO_FLOAT;
	}

	@Override
	public Integer visitBooleanParameter(Object arg) {
		return arg instanceof Boolean ? Integer.valueOf(0) : null;
	}

}
