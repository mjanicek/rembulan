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

abstract class ParameterMapping {

	static final ToLong TO_LONG = new ToLong();
	static final ToInt TO_INT = new ToInt();
	static final ToShort TO_SHORT = new ToShort();
	static final ToByte TO_BYTE = new ToByte();
	static final ToChar TO_CHAR = new ToChar();

	static final ToDouble TO_DOUBLE = new ToDouble();
	static final ToFloat TO_FLOAT = new ToFloat();

	static final ToBoolean TO_BOOLEAN = new ToBoolean();

	public abstract <T> T accept(ParameterMappingVisitor<T> visitor, Object arg);

	static ParameterMapping forClass(Class<?> paramClazz) {
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

	static ParameterMapping[] mappingsFor(Class<?>[] parameterTypes) {
		ParameterMapping[] result = new ParameterMapping[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			result[i] = forClass(parameterTypes[i]);
		}
		return result;
	}

	static final class Unwrap extends ParameterMapping {

		private final Class<?> clazz;

		public Unwrap(Class<?> clazz) {
			this.clazz = Objects.requireNonNull(clazz);
		}

		@Override
		public <T> T accept(ParameterMappingVisitor<T> visitor, Object arg) {
			return visitor.visitReferenceParameter(clazz, arg);
		}

	}

	static final class ToLong extends ParameterMapping {

		@Override
		public <T> T accept(ParameterMappingVisitor<T> visitor, Object arg) {
			return visitor.visitLongParameter(arg);
		}

	}

	static final class ToInt extends ParameterMapping {

		@Override
		public <T> T accept(ParameterMappingVisitor<T> visitor, Object arg) {
			return visitor.visitIntParameter(arg);
		}

	}

	static final class ToShort extends ParameterMapping {

		@Override
		public <T> T accept(ParameterMappingVisitor<T> visitor, Object arg) {
			return visitor.visitShortParameter(arg);
		}

	}

	static final class ToByte extends ParameterMapping {

		@Override
		public <T> T accept(ParameterMappingVisitor<T> visitor, Object arg) {
			return visitor.visitByteParameter(arg);
		}

	}

	static final class ToChar extends ParameterMapping {

		@Override
		public <T> T accept(ParameterMappingVisitor<T> visitor, Object arg) {
			return visitor.visitCharParameter(arg);
		}

	}

	static final class ToDouble extends ParameterMapping {

		@Override
		public <T> T accept(ParameterMappingVisitor<T> visitor, Object arg) {
			return visitor.visitDoubleParameter(arg);
		}

	}

	static final class ToFloat extends ParameterMapping {

		@Override
		public <T> T accept(ParameterMappingVisitor<T> visitor, Object arg) {
			return visitor.visitFloatParameter(arg);
		}

	}

	static final class ToBoolean extends ParameterMapping {

		@Override
		public <T> T accept(ParameterMappingVisitor<T> visitor, Object arg) {
			return visitor.visitBooleanParameter(arg);
		}

	}

}
