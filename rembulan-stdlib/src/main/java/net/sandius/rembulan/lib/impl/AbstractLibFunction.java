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

import net.sandius.rembulan.impl.NonsuspendableFunctionException;
import net.sandius.rembulan.runtime.AbstractFunctionAnyArg;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.util.Arrays;

public abstract class AbstractLibFunction extends AbstractFunctionAnyArg {

	protected abstract String name();

	@Override
	public void invoke(ExecutionContext context, Object[] args) throws ResolvedControlThrowable {
		ArgumentIterator callArgs = new ArgumentIterator(
				new NameMetamethodValueTypeNamer(context),
				name(),
				Arrays.copyOf(args, args.length));
		invoke(context, callArgs);
	}

	protected abstract void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable;

	@Override
	public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
		throw new NonsuspendableFunctionException(this.getClass());
	}

}
