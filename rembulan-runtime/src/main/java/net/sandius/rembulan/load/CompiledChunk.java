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

import java.util.Map;

/**
 * A compiled chunk, consisting of at least one Java class file.
 */
public interface CompiledChunk {

	/**
	 * Returns the contents of this chunk as a map from class names to class files
	 * (as byte vectors). To access the main class, use {@link #mainClassName()}.
	 *
	 * @return  a map from class names to class files (as byte vectors)
	 */
	Map<String, ByteVector> classMap();

	/**
	 * Returns the name of the main class of this compiled chunk.
	 *
	 * <p>The class name may be used as a key in the map obtained by {@link #classMap()};
	 * the map must contain an element with this key.</p>
	 *
	 * @return  the class name of the main class of this chunk
	 */
	String mainClassName();

}
