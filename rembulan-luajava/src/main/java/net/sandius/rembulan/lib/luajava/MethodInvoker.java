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

import net.sandius.rembulan.runtime.ReturnBuffer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class MethodInvoker {

	private final Method method;
	private final ValueConverter[] converters;
	private final int score;

	private MethodInvoker(Method method, ValueConverter[] converters, int score) {
		this.method = Objects.requireNonNull(method);
		this.converters = Objects.requireNonNull(converters);
		this.score = score;
	}

	private static MethodInvoker of(Method method, Object[] args) {
		int score = 0;

		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length != args.length) {
			// different parameter lengths
			return null;
		}

		ValueConverter[] converters = ValueConverter.convertersFor(paramTypes);

		assert (converters.length == args.length);

		// get overall score
		for (int i = 0; i < converters.length; i++) {
			ValueConverter conv = converters[i];

			int sc = conv.score(args[i]);
			if (sc < 0) {
				return null;  // rejected
			}
			score += sc;
		}

		return new MethodInvoker(method, converters, score);
	}

	public static MethodInvoker find(Class<?> clazz, String methodName, Object[] args) {

		// filter out non-matching methods

		int score = Integer.MAX_VALUE;
		List<MethodInvoker> invokers = new ArrayList<>();
		for (Method m : clazz.getMethods()) {
			if (m.getName().equals(methodName)) {
				MethodInvoker invoker = MethodInvoker.of(m, args);
				if (invoker != null) {
					if (invoker.score < score) {
						// the best so far
						invokers.clear();
						invokers.add(invoker);
					}
					else if (invoker.score == score) {
						// ambiguous call
						invokers.add(invoker);
					}
					else {
						// already have a better one than this
					}
				}
			}
		}

		if (invokers.size() > 1) {
			// ambiguous call
			throw new IllegalArgumentException("Ambiguous call to method '" + methodName + "'");
			// TODO: list conflicting methods
		}
		else if (invokers.size() == 0) {
			// no method found
			throw new IllegalArgumentException("No matching method for name '" + methodName + "'");
		}
		else {
			return invokers.get(0);
		}
	}

	public void invoke(ReturnBuffer buffer, Object instance, Object[] args)
			throws InvocationTargetException, IllegalAccessException {

		Object[] actualArgs = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			actualArgs[i] = converters[i].apply(args[i]);
		}

		Object result = method.invoke(instance, args);
		setResult(buffer, result);
	}

	private void setResult(ReturnBuffer buffer, Object result) {
		Class<?> returnType = method.getReturnType();
		if (void.class.equals(returnType)) {
			// no results
			buffer.setTo();
		}
		else {
			// TODO: unwrap arrays?
			buffer.setTo(JavaConversions.toLuaValue(result));
		}
	}

}
