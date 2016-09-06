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

/**
 * An exception thrown by the runtime when an attempt to perform an illegal operation.
 */
public class IllegalOperationAttemptException extends LuaRuntimeException {

	/**
	 * Constructs a new {@code IllegalOperationAttemptException} with the given error message.
	 *
	 * @param message  the error message
	 */
	public IllegalOperationAttemptException(String message) {
		super(message);
	}

	IllegalOperationAttemptException(String opName, String target) {
		this("attempt to " + opName + " a " + target + " value");
	}

	static IllegalOperationAttemptException arithmetic(Object a, Object b) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(Conversions.numericalValueOf(a) == null ? a : b);
		return new IllegalOperationAttemptException("perform arithmetic on", typeName);
	}

	static IllegalOperationAttemptException arithmetic(Object o) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(o);
		return new IllegalOperationAttemptException("perform arithmetic on", typeName);
	}

	static IllegalOperationAttemptException comparison(Object a, Object b) {
		String ta = PlainValueTypeNamer.INSTANCE.typeNameOf(a);
		String tb = PlainValueTypeNamer.INSTANCE.typeNameOf(b);
		String message = ta.equals(tb)
				? "attempt to compare two " + ta + " values"
				: "attempt to compare " + ta + " with " + tb;
		return new IllegalOperationAttemptException(message);
	}

	static IllegalOperationAttemptException call(Object o) {
		return new IllegalOperationAttemptException("call", PlainValueTypeNamer.INSTANCE.typeNameOf(o));
	}

	static IllegalOperationAttemptException index(Object table, Object key) {
		Object o = table instanceof Table ? key : table;
		return new IllegalOperationAttemptException("index", PlainValueTypeNamer.INSTANCE.typeNameOf(o));
	}

	static IllegalOperationAttemptException bitwise(Object a, Object b) {
		Object nonNumeric = Conversions.numericalValueOf(a) == null ? a : b;

		if (Conversions.numericalValueOf(nonNumeric) == null) {
			// indeed it's not a number
			String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(nonNumeric);
			return new IllegalOperationAttemptException("perform bitwise operation on", typeName);
		}
		else {
			return new IllegalOperationAttemptException("number has no integer representation");
		}
	}

	static IllegalOperationAttemptException bitwise(Object o) {
		if (Conversions.numericalValueOf(o) == null) {
			// indeed it's not a number
			String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(o);
			return new IllegalOperationAttemptException("perform bitwise operation on", typeName);
		}
		else {
			return new IllegalOperationAttemptException("number has no integer representation");
		}
	}

	static IllegalOperationAttemptException length(Object o) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(o);
		return new IllegalOperationAttemptException("attempt to get length of a " + typeName + " value");
	}

	static IllegalOperationAttemptException concatenate(Object a, Object b) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(Conversions.stringValueOf(a) == null ? a : b);
		return new IllegalOperationAttemptException("concatenate", typeName);
	}

}
