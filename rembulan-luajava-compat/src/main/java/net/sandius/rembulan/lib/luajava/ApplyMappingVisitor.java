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

class ApplyMappingVisitor implements ParameterMappingVisitor<Object> {

	static final ApplyMappingVisitor INSTANCE = new ApplyMappingVisitor();

	public static Object[] applyAll(ParameterMapping[] params, Object[] args) {
		Object[] result = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			result[i] = params[i].accept(INSTANCE, args[i]);
		}
		return result;
	}

	@Override
	public Object visitReferenceParameter(Class<?> clazz, Object arg) {
		return arg instanceof JavaWrapper
				? ((JavaWrapper<?>) arg).get()
				: arg;
	}

	@Override
	public Object visitLongParameter(Object arg) {
		return Conversions.integerValueOf(arg);
	}

	@Override
	public Object visitIntParameter(Object arg) {
		Long l = Conversions.integerValueOf(arg);
		return l != null ? Integer.valueOf(l.intValue()) : null;
	}

	@Override
	public Object visitShortParameter(Object arg) {
		Long l = Conversions.integerValueOf(arg);
		return l != null ? Short.valueOf(l.shortValue()) : null;
	}

	@Override
	public Object visitByteParameter(Object arg) {
		Long l = Conversions.integerValueOf(arg);
		return l != null ? Byte.valueOf(l.byteValue()) : null;
	}

	@Override
	public Object visitCharParameter(Object arg) {
		Long l = Conversions.integerValueOf(arg);
		return l != null ? Character.valueOf((char) l.intValue()) : null;
	}

	@Override
	public Object visitDoubleParameter(Object arg) {
		Number n = Conversions.numericalValueOf(arg);
		return n != null ? Conversions.floatValueOf(n) : null;
	}

	@Override
	public Object visitFloatParameter(Object arg) {
		Number n = Conversions.numericalValueOf(arg);
		return n != null ? Float.valueOf(n.floatValue()) : null;
	}

	@Override
	public Object visitBooleanParameter(Object arg) {
		return Conversions.booleanValueOf(arg);
	}

}
