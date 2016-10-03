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
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Userdata;
import net.sandius.rembulan.impl.NonsuspendableFunctionException;
import net.sandius.rembulan.lib.impl.NameMetamethodValueTypeNamer;
import net.sandius.rembulan.runtime.AbstractFunction1;
import net.sandius.rembulan.runtime.AbstractFunction2;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

abstract class JavaWrapper<T> extends Userdata {

	abstract String typeName();

	/**
	 * Returns the wrapped object.
	 *
	 * @return  the wrapped object
	 */
	public abstract T get();

	@Override
	public Object getUserValue() {
		return null;
	}

	@Override
	public Object setUserValue(Object value) {
		throw new UnsupportedOperationException("user value not supported");
	}

	@Override
	public Table setMetatable(Table mt) {
		throw new UnsupportedOperationException("cannot wrapper metatable");
	}

	static class ToString extends AbstractFunction1 {

		public static final ToString INSTANCE = new ToString();

		@Override
		public void invoke(ExecutionContext context, Object arg1) throws ResolvedControlThrowable {
			if (arg1 instanceof JavaWrapper) {
				JavaWrapper wrapper = (JavaWrapper) arg1;
				context.getReturnBuffer().setTo(wrapper.typeName() + " (" + wrapper.get().toString() + ")");
			}
			else {
				throw new IllegalArgumentException("invalid argument to toString: expecting Java wrapper, got "
						+ NameMetamethodValueTypeNamer.typeNameOf(arg1, context));
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	static abstract class AbstractGetMemberAccessor extends AbstractFunction2 {

		protected abstract LuaFunction accessorForName(String methodName);

		@Override
		public void invoke(ExecutionContext context, Object arg1, Object arg2)
				throws ResolvedControlThrowable {

			// arg1 is ignored

			final String methodName;
			{
				String s = Conversions.stringValueOf(arg2);
				if (s != null) {
					methodName = s;
				}
				else {
					throw new IllegalArgumentException("invalid member name: expecting string, got "
							+ NameMetamethodValueTypeNamer.typeNameOf(arg2, context));
				}
			}

			context.getReturnBuffer().setTo(accessorForName(methodName));
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

}
