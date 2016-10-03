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
import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.Metatables;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Userdata;
import net.sandius.rembulan.impl.ImmutableTable;
import net.sandius.rembulan.impl.NonsuspendableFunctionException;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.BasicLib;
import net.sandius.rembulan.lib.Lib;
import net.sandius.rembulan.lib.impl.NameMetamethodValueTypeNamer;
import net.sandius.rembulan.runtime.AbstractFunction1;
import net.sandius.rembulan.runtime.AbstractFunction2;
import net.sandius.rembulan.runtime.AbstractFunctionAnyArg;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

class ObjectWrapper extends Userdata {

	private final Object object;

	ObjectWrapper(Object object) {
		this.object = Objects.requireNonNull(object);
	}

	public static ObjectWrapper newInstance(Class<?> clazz, Object[] args)
			throws IllegalAccessException, InstantiationException, InvocationTargetException {
		ConstructorInvoker invoker = ConstructorInvoker.find(clazz, args);
		Object o = invoker.newInstance(args);
		return new ObjectWrapper(o);
	}

	public static ObjectWrapper newInstance(String className, Object[] args)
			throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
		return newInstance(Class.forName(className), args);
	}

	@Override
	public Table getMetatable() {
		return METATABLE;
	}

	@Override
	public Table setMetatable(Table mt) {
		throw new UnsupportedOperationException("cannot change object wrapper metatable");
	}

	@Override
	public Object getUserValue() {
		return null;  // TODO
	}

	@Override
	public Object setUserValue(Object value) {
		return null;  // TODO
	}

	/**
	 * Returns the wrapped object.
	 *
	 * @return  the wrapped object
	 */
	public Object get() {
		return object;
	}

	public void invoke(ExecutionContext context, String methodName, Object[] args) {
		MethodInvoker best = MethodInvoker.find(object.getClass(), methodName, args);

		try {
			best.invoke(context.getReturnBuffer(), object, args);
		}
		catch (IllegalAccessException | InvocationTargetException ex) {
			throw new LuaRuntimeException(ex);
		}
	}

	static final ImmutableTable METATABLE = new ImmutableTable.Builder()
			.add(Metatables.MT_INDEX, GetMethod.INSTANCE)
			.add(Lib.MT_NAME, "object wrapper")
			.add(BasicLib.MT_TOSTRING, ToString.INSTANCE)
			.build();

	static class GetMethod extends AbstractFunction2 {

		public static final GetMethod INSTANCE = new GetMethod();

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
					throw new IllegalArgumentException("invalid method name: expecting string, got "
							+ NameMetamethodValueTypeNamer.typeNameOf(arg2, context));
				}
			}

			context.getReturnBuffer().setTo(new InvokeJavaMethod(methodName));
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	static class ToString extends AbstractFunction1 {

		public static final ToString INSTANCE = new ToString();

		@Override
		public void invoke(ExecutionContext context, Object arg1) throws ResolvedControlThrowable {
			if (arg1 instanceof ObjectWrapper) {
				ObjectWrapper wrapper = (ObjectWrapper) arg1;
				context.getReturnBuffer().setTo("Java object (" + wrapper.get().toString() + ")");
			}
			else {
				throw new IllegalArgumentException("invalid argument to toString: expecting object wrapper, got "
						+ NameMetamethodValueTypeNamer.typeNameOf(arg1, context));
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	static class InvokeJavaMethod extends AbstractFunctionAnyArg {

		private final String methodName;

		public InvokeJavaMethod(String methodName) {
			this.methodName = Objects.requireNonNull(methodName);
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ResolvedControlThrowable {
			if (args.length < 1) {
				throw new BadArgumentException(1, methodName, "object wrapper expected, got no value");
			}

			final ObjectWrapper wrapper;
			{
				Object o = args[0];
				if (o instanceof ObjectWrapper) {
					wrapper = (ObjectWrapper) o;
				}
				else {
					throw new BadArgumentException(1, methodName, "object wrapper expected, got "
							+ NameMetamethodValueTypeNamer.typeNameOf(o, context));
				}
			}

			Object instance = wrapper.get();

			Object[] invokeArgs = new Object[args.length - 1];
			System.arraycopy(args, 1, invokeArgs, 0, invokeArgs.length);

			// find the best method invoker
			MethodInvoker invoker = MethodInvoker.find(instance.getClass(), methodName, invokeArgs);

			// invoke the method
			try {
				invoker.invoke(context.getReturnBuffer(), instance, invokeArgs);
			}
			catch (InvocationTargetException | IllegalAccessException ex) {
				throw new LuaRuntimeException(ex);
			}

		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}


}
