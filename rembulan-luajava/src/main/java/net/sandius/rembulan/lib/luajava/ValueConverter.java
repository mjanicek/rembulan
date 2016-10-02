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

import java.util.Objects;

abstract class ValueConverter {

	static final ToLong TO_LONG = new ToLong();
	static final ToInt TO_INT = new ToInt();
	static final ToShort TO_SHORT = new ToShort();
	static final ToByte TO_BYTE = new ToByte();
	static final ToChar TO_CHAR = new ToChar();

	static final ToDouble TO_DOUBLE = new ToDouble();
	static final ToFloat TO_FLOAT = new ToFloat();

	static final ToBoolean TO_BOOLEAN = new ToBoolean();

	public abstract Object apply(Object arg);

	public abstract int score(Object arg);

	static ValueConverter forClass(Class<?> paramClazz) {
		// primitive types
		if (long.class.equals(paramClazz)) return TO_LONG;
		else if (int.class.equals(paramClazz)) return TO_INT;
		else if (short.class.equals(paramClazz)) return TO_SHORT;
		else if (byte.class.equals(paramClazz)) return TO_BYTE;
		else if (char.class.equals(paramClazz)) return TO_CHAR;
		else if (double.class.equals(paramClazz)) return TO_DOUBLE;
		else if (float.class.equals(paramClazz)) return TO_FLOAT;
		else if (boolean.class.equals(paramClazz)) return TO_BOOLEAN;

		// reference type
		else return new Unwrap(paramClazz);
	}

	static ValueConverter[] convertersFor(Class<?>[] paramTypes) {
		ValueConverter[] result = new ValueConverter[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			result[i] = forClass(paramTypes[i]);
		}
		return result;
	}

	static final class Unwrap extends ValueConverter {

		private final Class<?> clazz;

		public Unwrap(Class<?> clazz) {
			this.clazz = Objects.requireNonNull(clazz);
		}

		@Override
		public Object apply(Object arg) {
			if (arg instanceof ObjectWrapper) {
				return ((ObjectWrapper) arg).get();
			}
			else if (arg instanceof ClassWrapper) {
				return ((ClassWrapper) arg).get();
			}
			else {
				return arg;
			}
		}

		@Override
		public int score(Object arg) {
			if (arg != null) {
				if (arg instanceof ObjectWrapper) {
					return score(((ObjectWrapper) arg).get());
				}
				else if (arg instanceof ClassWrapper) {
					return score(((ClassWrapper) arg).get());
				}
				else {
					if (clazz.isAssignableFrom(arg.getClass())) {
						return 0;
					}
					else {
						// clazz is not the same class or a superclass of arg
						return -1;
					}
				}
			}
			else {
				// argument is nil, parameter is non-primitive
				return 0;
			}
		}

	}

	private static Object check(Object o, Object original) {
		if (o != null) {
			return o;
		}
		else {
			throw new IllegalArgumentException("Illegal value: " + original);
		}
	}

	static final class ToLong extends ValueConverter {

		@Override
		public Object apply(Object arg) {
			return check(JavaConversions.toLong(arg), arg);
		}

		@Override
		public int score(Object arg) {
			if (arg instanceof Long) return 0;
			else if (arg instanceof Number) return 1;
			else return 2;
		}

	}

	static final class ToInt extends ValueConverter {

		@Override
		public Object apply(Object arg) {
			return check(JavaConversions.toInteger(arg), arg);
		}

		@Override
		public int score(Object arg) {
			if (arg instanceof Long) return 1;
			else if (arg instanceof Number) return 2;
			else return 3;
		}

	}

	static final class ToShort extends ValueConverter {

		@Override
		public Object apply(Object arg) {
			return check(JavaConversions.toShort(arg), arg);
		}

		@Override
		public int score(Object arg) {
			if (arg instanceof Long) return 1;
			else if (arg instanceof Number) return 2;
			else return 3;
		}

	}

	static final class ToByte extends ValueConverter {

		@Override
		public Object apply(Object arg) {
			return check(JavaConversions.toByte(arg), arg);
		}

		@Override
		public int score(Object arg) {
			if (arg instanceof Long) return 1;
			else if (arg instanceof Number) return 2;
			else return 3;
		}

	}

	static final class ToChar extends ValueConverter {

		@Override
		public Object apply(Object arg) {
			return check(JavaConversions.toChar(arg), arg);
		}

		@Override
		public int score(Object arg) {
			if (arg instanceof Long) return 1;
			else if (arg instanceof Number) return 2;
			else return 3;
		}

	}

	static final class ToDouble extends ValueConverter {

		@Override
		public Object apply(Object arg) {
			return check(JavaConversions.toDouble(arg), arg);
		}

		@Override
		public int score(Object arg) {
			if (arg instanceof Double) return 0;
			else if (arg instanceof Number) return 1;
			else return 2;
		}

	}

	static final class ToFloat extends ValueConverter {

		@Override
		public Object apply(Object arg) {
			return check(JavaConversions.toFloat(arg), arg);
		}

		@Override
		public int score(Object arg) {
			if (arg instanceof Double) return 1;
			else if (arg instanceof Number) return 2;
			else return 3;
		}

	}

	static final class ToBoolean extends ValueConverter {

		@Override
		public Object apply(Object arg) {
			return check(JavaConversions.toBoolean(arg), arg);
		}

		@Override
		public int score(Object arg) {
			if (arg instanceof Boolean) return 0;
			else return 1;
		}

	}

}
