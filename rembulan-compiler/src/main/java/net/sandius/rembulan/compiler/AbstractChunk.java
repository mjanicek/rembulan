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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractChunk {

	@Deprecated
	public abstract Iterable<CompiledClass> classes();

	public Map<String, ByteVector> classMap() {
		Map<String, ByteVector> m = new HashMap<>();
		for (CompiledClass cc : classes()) {
			m.put(cc.name(), cc.bytes());
		}
		return Collections.unmodifiableMap(m);
	}

	public abstract String mainClassName();

}
