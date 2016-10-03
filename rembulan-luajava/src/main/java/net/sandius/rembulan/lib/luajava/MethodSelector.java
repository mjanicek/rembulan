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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class MethodSelector {

	private static Integer distance(List<ParameterMapping> parameterMappings, Object[] arguments) {
		if (parameterMappings.size() != arguments.length) {
			return null;
		}

		int result = 0;

		for (int i = 0; i < parameterMappings.size(); i++) {
			Integer d = parameterMappings.get(i).accept(DistanceMappingVisitor.INSTANCE, arguments[i]);
			if (d != null) {
				result += d;
			}
			else {
				return null;
			}
		}

		return result;
	}

	public static <T> MappedConstructor<T> selectConstructor(Class<T> clazz, Object[] arguments)
				throws MethodSelectionException {

		// filter out non-matching methods

		int distance = Integer.MAX_VALUE;
		List<MappedConstructor<T>> best = new ArrayList<>();
		for (Constructor<?> ctor : clazz.getConstructors()) {

			@SuppressWarnings("unchecked")
			MappedConstructor<T> invoker = MappedConstructor.of((Constructor<T>) ctor);

			Integer d = distance(invoker.parameterMappings(), arguments);
			if (d != null) {
				int dd = d;
				if (dd < distance) {
					// the best so far
					best.clear();
					best.add(invoker);
					distance = dd;
				}
				else if (dd == distance) {
					// ambiguous call
					best.add(invoker);
				}
				else {
					// already have a better one than this
				}
			}
		}

		if (best.size() > 1) {
			// ambiguous call
			throw new MethodSelectionException("Ambiguous constructor for class " + clazz.getName());
			// TODO: list conflicting methods
		}
		else if (best.size() == 0) {
			// no method found
			throw new MethodSelectionException("No matching constructor for class " + clazz.getName());
		}
		else {
			return best.get(0);
		}

	}

	public static MappedMethod select(Class<?> clazz, String methodName, boolean isStatic, Object[] arguments)
			throws MethodSelectionException {

		int distance = Integer.MAX_VALUE;
		List<MappedMethod> best = new ArrayList<>();

		for (Method m : clazz.getMethods()) {
			int mod = m.getModifiers();

			// filter out non-matching methods
			if (Modifier.isPublic(mod)
					&& Modifier.isStatic(mod) == isStatic
					&& m.getName().equals(methodName)) {

				MappedMethod invoker = MappedMethod.of(m);

				Integer d = distance(invoker.parameterMappings(), arguments);
				if (d != null) {
					int dd = d;
					if (dd < distance) {
						// the best so far
						best.clear();
						best.add(invoker);
						distance = dd;
					}
					else if (dd == distance) {
						// ambiguous call
						best.add(invoker);
					}
					else {
						// already have a better one than this
					}
				}
			}
		}

		if (best.size() > 1) {
			// ambiguous call

			StringBuilder bld = new StringBuilder();
			bld.append("Ambiguous call to method '").append(methodName).append("', candidates are:").append('\n');
			Iterator<MappedMethod> it = best.iterator();
			while (it.hasNext()) {
				MappedMethod m = it.next();
				bld.append('\t').append(m.method());
				if (it.hasNext()) {
					bld.append('\n');
				}
			}
			throw new MethodSelectionException(bld.toString());
		}
		else if (best.size() == 0) {
			// no method found
			throw new IllegalArgumentException("No matching method for name '" + methodName + "'");
		}
		else {
			return best.get(0);
		}
	}

}
