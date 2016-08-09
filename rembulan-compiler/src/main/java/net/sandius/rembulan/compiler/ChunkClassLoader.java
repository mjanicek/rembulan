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

import net.sandius.rembulan.util.ByteVector;

import java.util.HashMap;
import java.util.Map;

public class ChunkClassLoader extends ClassLoader {

	private final Map<String, ByteVector> installed;

	public ChunkClassLoader(ClassLoader parent) {
		super(parent);
		this.installed = new HashMap<>();
	}

	public ChunkClassLoader() {
		this(ChunkClassLoader.class.getClassLoader());
	}

	public String install(AbstractChunk chunk) {
		Map<String, ByteVector> classes = chunk.classMap();

		for (String name : classes.keySet()) {
			if (installed.containsKey(name)) {
				// class already installed
				throw new IllegalArgumentException();  // TODO: throw the right exception
			}

			installed.put(name, classes.get(name));
		}

		String main = chunk.mainClassName();
		assert (installed.containsKey(main));
		return main;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		ByteVector bv = installed.get(name);

		if (bv != null) {
			// no need to keep it any longer (TODO: thread-safety)
			installed.remove(name);
			return defineClass(name, bv);
		}
		else {
			throw new ClassNotFoundException(name);
		}
	}

	private Class<?> defineClass(String name, ByteVector bytes) {
		byte[] byteArray = bytes.copyToNewArray();
		return defineClass(name, byteArray, 0, byteArray.length);
	}

}
