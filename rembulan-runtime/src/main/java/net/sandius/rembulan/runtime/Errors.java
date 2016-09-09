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

package net.sandius.rembulan.runtime;

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.NoIntegerRepresentationException;
import net.sandius.rembulan.PlainValueTypeNamer;
import net.sandius.rembulan.Table;

final class Errors {

	private Errors() {
		// not to be instantiated
	}

	static NoIntegerRepresentationException noIntegerRepresentation() {
		return new NoIntegerRepresentationException();
	}

	private static String attemptTemplateMessage(String opName, String target) {
		return "attempt to " + opName + " a " + target + " value";
	}

	static IllegalOperationAttemptException illegalArithmeticAttempt(Object a, Object b) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(Conversions.numericalValueOf(a) == null ? a : b);
		return new IllegalOperationAttemptException(attemptTemplateMessage("perform arithmetic on", typeName));
	}

	static IllegalOperationAttemptException illegalArithmeticAttempt(Object o) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(o);
		return new IllegalOperationAttemptException(attemptTemplateMessage("perform arithmetic on", typeName));
	}

	static IllegalOperationAttemptException illegalComparisonAttempt(Object a, Object b) {
		String ta = PlainValueTypeNamer.INSTANCE.typeNameOf(a);
		String tb = PlainValueTypeNamer.INSTANCE.typeNameOf(b);
		String message = ta.equals(tb)
				? "attempt to compare two " + ta + " values"
				: "attempt to compare " + ta + " with " + tb;
		return new IllegalOperationAttemptException(message);
	}

	static IllegalOperationAttemptException illegalCallAttempt(Object o) {
		return new IllegalOperationAttemptException(
				attemptTemplateMessage("call", PlainValueTypeNamer.INSTANCE.typeNameOf(o)));
	}

	static IllegalOperationAttemptException illegalIndexAttempt(Object table, Object key) {
		Object o = table instanceof Table ? key : table;
		return new IllegalOperationAttemptException(
				attemptTemplateMessage("index", PlainValueTypeNamer.INSTANCE.typeNameOf(o)));
	}

	static IllegalOperationAttemptException illegalBitwiseOperationAttempt(Object a, Object b) {
		Object nonNumeric = Conversions.numericalValueOf(a) == null ? a : b;

		if (Conversions.numericalValueOf(nonNumeric) == null) {
			// indeed it's not a number
			String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(nonNumeric);
			return new IllegalOperationAttemptException(
					attemptTemplateMessage("perform bitwise operation on", typeName));
		}
		else {
			return new IllegalOperationAttemptException(Errors.noIntegerRepresentation());
		}
	}

	static IllegalOperationAttemptException illegalBitwiseOperationAttempt(Object o) {
		if (Conversions.numericalValueOf(o) == null) {
			// indeed it's not a number
			String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(o);
			return new IllegalOperationAttemptException(
					attemptTemplateMessage("perform bitwise operation on", typeName));
		}
		else {
			return new IllegalOperationAttemptException(Errors.noIntegerRepresentation());
		}
	}

	static IllegalOperationAttemptException illegalGetLengthAttempt(Object o) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(o);
		return new IllegalOperationAttemptException("attempt to get length of a " + typeName + " value");
	}

	static IllegalOperationAttemptException illegalConcatenationAttempt(Object a, Object b) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(Conversions.stringValueOf(a) == null ? a : b);
		return new IllegalOperationAttemptException(
				attemptTemplateMessage("concatenate", typeName));
	}

	static IllegalCoroutineStateException illegalYieldAttempt() {
		return new IllegalCoroutineStateException("attempt to yield from outside a coroutine");
	}

	static IllegalCoroutineStateException resumeDeadCoroutine() {
		return new IllegalCoroutineStateException("cannot resume dead coroutine");
	}

	static IllegalCoroutineStateException resumeNonSuspendedCoroutine() {
		return new IllegalCoroutineStateException("cannot resume non-suspended coroutine");
	}

}
