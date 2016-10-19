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

import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.Metatables;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.impl.ImmutableTable;
import net.sandius.rembulan.impl.NonsuspendableFunctionException;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.Lib;
import net.sandius.rembulan.lib.impl.DefaultBasicLib;
import net.sandius.rembulan.lib.impl.NameMetamethodValueTypeNamer;
import net.sandius.rembulan.runtime.AbstractFunctionAnyArg;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

final class ObjectWrapper<T> extends JavaWrapper<T> {

	private final T instance;

	private ObjectWrapper(T instance) {
		this.instance = Objects.requireNonNull(instance);
	}

	public static <T> ObjectWrapper<T> of(T instance) {
		return new ObjectWrapper<>(instance);
	}

	public static <T> ObjectWrapper<T> newInstance(Class<T> clazz, Object[] args)
			throws MethodSelectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
		MappedConstructor<T> invoker = MethodSelector.selectConstructor(clazz, args);
		T o = invoker.newInstance(args);
		return new ObjectWrapper<>(o);
	}

	public static ObjectWrapper<?> newInstance(String className, Object[] args)
			throws MethodSelectionException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
		return newInstance(Class.forName(className), args);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ObjectWrapper<?> that = (ObjectWrapper<?>) o;
		return this.instance == that.instance;
	}

	@Override
	public int hashCode() {
		return instance.hashCode();
	}

	static String staticTypeName() {
		return "Java object";
	}

	@Override
	String typeName() {
		return staticTypeName();
	}

	@Override
	public Table getMetatable() {
		return METATABLE;
	}

	@Override
	public T get() {
		return instance;
	}

	static final ImmutableTable METATABLE = new ImmutableTable.Builder()
			.add(Metatables.MT_INDEX, GetInstanceMemberAccessor.INSTANCE)
			.add(Lib.MT_NAME, staticTypeName())
			.add(DefaultBasicLib.MT_TOSTRING, ToString.INSTANCE)
			.build();

	static class GetInstanceMemberAccessor extends AbstractGetMemberAccessor {

		public static final GetInstanceMemberAccessor INSTANCE = new GetInstanceMemberAccessor();

		@Override
		protected LuaFunction methodAccessorForName(String methodName) {
			return new InvokeInstanceMethod(methodName);
		}

	}

	static class InvokeInstanceMethod extends AbstractFunctionAnyArg {

		private final String methodName;

		public InvokeInstanceMethod(String methodName) {
			this.methodName = Objects.requireNonNull(methodName);
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ResolvedControlThrowable {
			if (args.length < 1) {
				throw new BadArgumentException(1, methodName, staticTypeName() + " expected, got no value");
			}

			final ObjectWrapper<?> wrapper;
			{
				Object o = args[0];
				if (o instanceof ObjectWrapper) {
					wrapper = (ObjectWrapper<?>) o;
				}
				else {
					throw new BadArgumentException(1, methodName, staticTypeName() + ", got "
							+ NameMetamethodValueTypeNamer.typeNameOf(o, context));
				}
			}

			Object instance = wrapper.get();

			Object[] invokeArgs = new Object[args.length - 1];
			System.arraycopy(args, 1, invokeArgs, 0, invokeArgs.length);

			try {
				// find the best method invoker
				MappedMethod invoker = MethodSelector.select(instance.getClass(), methodName, false, invokeArgs);

				// invoke the method
				invoker.invoke(context.getReturnBuffer(), instance, invokeArgs);
			}
			catch (MethodSelectionException | InvocationTargetException | IllegalAccessException ex) {
				throw new LuaRuntimeException(ex);
			}

		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}


}
