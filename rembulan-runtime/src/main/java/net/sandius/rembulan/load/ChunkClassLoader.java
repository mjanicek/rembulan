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

package net.sandius.rembulan.load;

import net.sandius.rembulan.util.ByteVector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class loader for Lua chunks.
 */
public class ChunkClassLoader extends ClassLoader {

	private final Map<String, ByteVector> installed;
	private final Set<String> loaded;

	/**
	 * Constructs a new {@code ChunkClassLoader} with the specified class loader {@code parent}
	 * as its parent in the class loading hierarchy.
	 *
	 * @param parent  the parent class loader
	 */
	public ChunkClassLoader(ClassLoader parent) {
		super(parent);
		this.installed = new HashMap<>();
		this.loaded = new HashSet<>();
	}

	/**
	 * Constructs a new {@code ChunkClassLoader} using the class loader that loaded
	 * the {@code ChunkClassLoader} class as the parent class loader.
	 */
	public ChunkClassLoader() {
		this(ChunkClassLoader.class.getClassLoader());
	}

	/**
	 * Installs the compiled chunk {@code chunk} into this chunk class loader, returning
	 * the class name of the main class in {@code chunk}.
	 *
	 * <p>Once this method returns (and not before), the returned main class may be loaded
	 * and instantiated.</p>
	 *
	 * @param chunk  chunk to be installed, must not be {@code null}
	 * @return  the class name of the main class in {@code chunk}
	 *
	 * @throws NullPointerException  if {@code chunk} is {@code null}
	 * @throws IllegalStateException  if a class with the same name as a class in {@code chunk}
	 *                                has already been installed into this chunk class loader
	 *
	 */
	public String install(CompiledChunk chunk) {
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
	 * has been installed into this {@code ChunkClassLoader} or has already been loaded by it.
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
