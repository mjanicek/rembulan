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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkClassLoader extends ClassLoader {

	private final Map<String, ByteVector> installed;
	private final Set<String> loaded;

	public ChunkClassLoader(ClassLoader parent) {
		super(parent);
		this.installed = new HashMap<>();
		this.loaded = new HashSet<>();
	}

	public ChunkClassLoader() {
		this(ChunkClassLoader.class.getClassLoader());
	}

	public String install(AbstractChunk chunk) {
		Map<String, ByteVector> classes = chunk.classMap();

		synchronized (this) {
			for (String name : classes.keySet()) {
				if (installed.containsKey(name) || loaded.contains(name)) {
					// class already installed
					throw new IllegalStateException("Class already installed: " + name);
				}

				installed.put(name, classes.get(name));
			}

			String main = chunk.mainClassName();
			assert (installed.containsKey(main));
			return main;
		}
	}

	/**
	 * Returns {@code true} if the Lua function class with the given {@code className}
	 * has been installed into this ChunkClassLoader has already been loaded by it.
	 *
	 * @param className  class name of the Lua function, must not be {@code null}
	 * @return  {@code true} iff the class {@code className} has been installed into this
	 *          class loader or loaded by it
	 * @throws NullPointerException  if {@code className} is {@code null}
	 */
	public boolean isInstalled(String className) {
		synchronized (this) {
			return installed.containsKey(className) || loaded.contains(className);
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		synchronized (this) {
			ByteVector bv = installed.remove(name);
			if (bv != null) {
				loaded.add(name);
				return defineClass(name, bv);
			}
			else {
				throw new ClassNotFoundException(name);
			}
		}
	}

	private Class<?> defineClass(String name, ByteVector bytes) {
		byte[] byteArray = bytes.copyToNewArray();
		return defineClass(name, byteArray, 0, byteArray.length);
	}

}
