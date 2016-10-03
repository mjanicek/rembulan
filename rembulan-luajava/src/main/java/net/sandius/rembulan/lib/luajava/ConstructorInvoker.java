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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class ConstructorInvoker<T> {

	private final Constructor<T> constructor;
	private final ValueConverter[] converters;

	private final int score;

	ConstructorInvoker(Constructor<T> constructor, ValueConverter[] converters, int score) {
		this.constructor = Objects.requireNonNull(constructor);
		this.converters = converters;
		this.score = score;
	}

	static <T> ConstructorInvoker<T> of(Constructor<T> constructor, Object[] args) {
		int score = 0;

		Class<?>[] paramTypes = constructor.getParameterTypes();
		if (paramTypes.length != args.length) {
			// different parameter lengths
			return null;
		}

		ValueConverter[] converters = ValueConverter.convertersFor(paramTypes);

		// get overall score
		for (int i = 0; i < converters.length; i++) {
			ValueConverter conv = converters[i];

			int sc = conv.score(args[i]);
			if (sc < 0) {
				return null;  // rejected
			}
			score += sc;
		}

		return new ConstructorInvoker<>(constructor, converters, score);
	}

	public static <T> ConstructorInvoker<T> find(Class<T> clazz, Object[] args) {

		// filter out non-matching methods

		int score = Integer.MAX_VALUE;
		List<ConstructorInvoker<T>> invokers = new ArrayList<>();
		for (Constructor<?> ctor : clazz.getConstructors()) {

			@SuppressWarnings("unchecked")
			ConstructorInvoker<T> invoker = ConstructorInvoker.of((Constructor<T>) ctor, args);

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
					// ignore
				}
			}
		}

		if (invokers.size() > 1) {
			// ambiguous call
			throw new IllegalArgumentException("Ambiguous constructor for class " + clazz.getName());
			// TODO: list conflicting methods
		}
		else if (invokers.size() == 0) {
			// no method found
			throw new IllegalArgumentException("No matching constructor for class " + clazz.getName());
		}
		else {
			return invokers.get(0);
		}
	}

	public T newInstance(Object[] args)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {

		Object[] actualArgs = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			actualArgs[i] = converters[i].apply(args[i]);
		}

		return constructor.newInstance(actualArgs);
	}

}
