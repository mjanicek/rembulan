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

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.impl.AbstractFunctionAnyArg;

public abstract class AbstractLibFunction extends AbstractFunctionAnyArg {

	protected abstract String name();

	@Override
	public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
		ArgumentIterator callArgs = new ArgumentIterator(new NameMetamethodValueTypeNamer(context.getState()), name(), args);
		invoke(context, callArgs);
	}

	protected abstract void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable;

	@Override
	public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
		throw new NonsuspendableFunctionException(this.getClass());
	}

}
