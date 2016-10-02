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
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Userdata;
import net.sandius.rembulan.runtime.ExecutionContext;

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
		return null;  // TODO
	}

	@Override
	public Table setMetatable(Table mt) {
		return null;  // TODO
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

}
