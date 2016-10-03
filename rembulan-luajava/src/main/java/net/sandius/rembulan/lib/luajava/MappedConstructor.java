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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class MappedConstructor<T> {

	private final Constructor<T> constructor;
	private final ParameterMapping[] parameterMappings;

	MappedConstructor(Constructor<T> constructor, ParameterMapping[] parameterMappings) {
		this.constructor = Objects.requireNonNull(constructor);
		this.parameterMappings = Objects.requireNonNull(parameterMappings);
	}

	static <T> MappedConstructor<T> of(Constructor<T> constructor) {
		ParameterMapping[] mappings = ParameterMapping.mappingsFor(constructor.getParameterTypes());
		return new MappedConstructor<>(constructor, mappings);
	}

	public Constructor<T> constructor() {
		return constructor;
	}

	public List<ParameterMapping> parameterMappings() {
		return Collections.unmodifiableList(Arrays.asList(parameterMappings));
	}

	public T newInstance(Object[] args)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {

		Object[] actualArgs = ApplyMappingVisitor.applyAll(parameterMappings, args);
		return constructor.newInstance(actualArgs);
	}

}
