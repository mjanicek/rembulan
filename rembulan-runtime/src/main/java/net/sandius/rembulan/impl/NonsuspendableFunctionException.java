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

package net.sandius.rembulan.impl;

import net.sandius.rembulan.Function;

/**
 * Exception thrown to indicate that a function is not suspendable.
 */
public class NonsuspendableFunctionException extends UnsupportedOperationException {

	/**
	 * Constructs a new instance of {@code NonsuspendableFunctionException} with the argument
	 * {@code clazz} indicating the class of the function (for inclusion in the error message).
	 *
	 * <p>The argument may be {@code null}, in which case the function class name is not
	 * included in the error message.</p>
	 *
	 * @param clazz  class of the function, may be {@code null}
	 */
	public NonsuspendableFunctionException(Class<? extends Function> clazz) {
		super("Function is not suspendable" + (clazz != null
				? ": " + clazz.getName()
				: ""));
	}

	/**
	 * Constructs a new instance of {@code NonsuspendableFunctionException} without
	 * indicating the function class.
	 */
	public NonsuspendableFunctionException() {
		this(null);
	}

}
