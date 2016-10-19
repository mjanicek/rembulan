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
import net.sandius.rembulan.lib.BasicLib;
import net.sandius.rembulan.lib.Lib;
import net.sandius.rembulan.lib.NameMetamethodValueTypeNamer;
import net.sandius.rembulan.runtime.AbstractFunctionAnyArg;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

final class ClassWrapper<T> extends JavaWrapper<Class<T>> {

	private final Class<T> clazz;

	private ClassWrapper(Class<T> clazz) {
		this.clazz = Objects.requireNonNull(clazz);
	}

	public static <T> ClassWrapper<T> of(Class<T> clazz) {
		return new ClassWrapper<>(clazz);
	}

	public static ClassWrapper<?> of(String className, ClassLoader classLoader)
			throws ClassNotFoundException {

		return new ClassWrapper<>(Class.forName(className, true, classLoader));
	}

	public static ClassWrapper<?> of(String className)
			throws ClassNotFoundException {
		return of(className, ClassWrapper.class.getClassLoader());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassWrapper<?> that = (ClassWrapper<?>) o;
		return clazz.equals(that.clazz);
	}

	@Override
	public int hashCode() {
		return clazz.hashCode();
	}

	static String staticTypeName() {
		return "Java class";
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
	public Class<T> get() {
		return clazz;
	}

	static final ImmutableTable METATABLE = new ImmutableTable.Builder()
			.add(Metatables.MT_INDEX, GetStaticMemberAccessor.INSTANCE)
			.add(Lib.MT_NAME, staticTypeName())
			.add(BasicLib.MT_TOSTRING, ToString.INSTANCE)
			.build();

	static class GetStaticMemberAccessor extends AbstractGetMemberAccessor {

		public static final GetStaticMemberAccessor INSTANCE = new GetStaticMemberAccessor();

		@Override
		protected LuaFunction methodAccessorForName(String methodName) {
			return new InvokeStaticMethod(methodName);
		}

	}

	static class InvokeStaticMethod extends AbstractFunctionAnyArg {

		private final String methodName;

		public InvokeStaticMethod(String methodName) {
			this.methodName = Objects.requireNonNull(methodName);
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ResolvedControlThrowable {
			if (args.length < 1) {
				throw new BadArgumentException(1, methodName, staticTypeName() + " expected, got no value");
			}

			final ClassWrapper<?> wrapper;
			{
				Object o = args[0];
				if (o instanceof ClassWrapper) {
					wrapper = (ClassWrapper<?>) o;
				}
				else {
					throw new BadArgumentException(1, methodName, staticTypeName() + ", got "
							+ NameMetamethodValueTypeNamer.typeNameOf(o, context));
				}
			}

			Object[] invokeArgs = new Object[args.length - 1];
			System.arraycopy(args, 1, invokeArgs, 0, invokeArgs.length);

			try {
				// find the best method invoker
				MappedMethod invoker = MethodSelector.select(wrapper.get(), methodName, true, invokeArgs);

				// invoke the method
				invoker.invoke(context.getReturnBuffer(), null, invokeArgs);
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
