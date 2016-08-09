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

package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.gen.CompiledClass;
import net.sandius.rembulan.util.ByteVector;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompiledModule extends AbstractChunk {

	private final Map<String, ByteVector> classMap;
	private final String mainClassName;

	public CompiledModule(Map<String, ByteVector> classMap, String mainClassName) {
		this.classMap = Check.notNull(classMap);
		this.mainClassName = Check.notNull(mainClassName);

		if (!classMap.containsKey(mainClassName)) {
			throw new IllegalStateException("No main class in class map");
		}
	}

	@Override
	public Map<String, ByteVector> classMap() {
		return classMap;
	}

	@Override
	public Iterable<CompiledClass> classes() {
		List<CompiledClass> result = new ArrayList<>();
		for (Map.Entry<String, ByteVector> e : classMap.entrySet()) {
			result.add(new CompiledClass(e.getKey(), e.getValue()));
		}
		return result;
	}

	@Override
	public String mainClassName() {
		return mainClassName;
	}

}
