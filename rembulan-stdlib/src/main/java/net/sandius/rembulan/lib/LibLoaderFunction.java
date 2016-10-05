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

package net.sandius.rembulan.lib;

import net.sandius.rembulan.Table;
import net.sandius.rembulan.lib.impl.AbstractLibFunction;
import net.sandius.rembulan.lib.impl.ArgumentIterator;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.util.Objects;

public class LibLoaderFunction extends AbstractLibFunction {

	private final Table env;
	private final Lib lib;

	public LibLoaderFunction(Table env, Lib lib) {
		this.env = Objects.requireNonNull(env);
		this.lib = Objects.requireNonNull(lib);
	}

	@Override
	protected String name() {
		String n = lib.name();
		return "(loader for " + (n != null ? n : "?") + ")";
	}

	@Override
	protected void invoke(ExecutionContext context, ArgumentIterator args)
			throws ResolvedControlThrowable {

		String modName = args.nextString();
		String origin = args.hasNext() ? args.nextString() : null;

		Object result = lib.install(context, env, modName, origin);

		if (result != null) {
			context.getReturnBuffer().setTo(result);
		}
		else {
			context.getReturnBuffer().setTo();
		}

	}

}
