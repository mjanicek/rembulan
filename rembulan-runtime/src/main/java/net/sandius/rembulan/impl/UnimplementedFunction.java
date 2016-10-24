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

import net.sandius.rembulan.runtime.AbstractFunction0;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

/**
 * A function that throws an {@link UnsupportedOperationException} when invoked,
 * for use as a placeholder for unimplemented functions.
 */
public class UnimplementedFunction extends AbstractFunction0 {

	private final String name;

	/**
	 * Constructs a new instance of {@code UnimplementedFunction} with the given name
	 * for error reporting. {@code name} may be {@code null}, in which case no name will
	 * be included in error messages.
	 *
	 * @param name  function name for error reporting, may be {@code null}
	 */
	public UnimplementedFunction(String name) {
		this.name = name;
	}

	/**
	 * Constructs a new instance of {@code UnimplementedFunction} without a name for error
	 * reporting. Equivalent to {@link #UnimplementedFunction(String) UnimplementedFunction(null)}.
	 */
	public UnimplementedFunction() {
		this(null);
	}

	@Override
	public void invoke(ExecutionContext context) throws ResolvedControlThrowable {
		throw new UnsupportedOperationException("function not implemented" + (name != null ? ": " + name : ""));
	}

	@Override
	public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
		throw new NonsuspendableFunctionException(this.getClass());
	}

}
