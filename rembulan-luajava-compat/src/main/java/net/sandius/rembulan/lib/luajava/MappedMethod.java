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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class MappedMethod {

	private final Method method;
	private final ParameterMapping[] parameterMappings;

	private MappedMethod(Method method, ParameterMapping[] parameterMappings) {
		this.method = Objects.requireNonNull(method);
		this.parameterMappings = Objects.requireNonNull(parameterMappings);
	}

	public static MappedMethod of(Method method) {
		ParameterMapping[] converters = ParameterMapping.mappingsFor(method.getParameterTypes());
		return new MappedMethod(method, converters);
	}

	public Method method() {
		return method;
	}

	public List<ParameterMapping> parameterMappings() {
		return Collections.unmodifiableList(Arrays.asList(parameterMappings));
	}

	public void invoke(ReturnBuffer buffer, Object instance, Object[] args)
			throws InvocationTargetException, IllegalAccessException {

		// convert the call arguments
		Object[] actualArgs = ApplyMappingVisitor.applyAll(parameterMappings, args);

		// invoke
		Object result = method.invoke(instance, actualArgs);

		// set result
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
			buffer.setTo(Unmapper.unmapFrom(result));
		}
	}

}
