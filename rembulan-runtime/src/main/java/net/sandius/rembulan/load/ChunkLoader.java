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

import net.sandius.rembulan.Variable;
import net.sandius.rembulan.runtime.Function;

/**
 * Lua chunk loader, an object that converts the textual representation of Lua programs
 * into instances of {@code Function}.
 */
public interface ChunkLoader {

	/**
	 * Loads the text chunk {@code chunk} (a string containing a Lua program) and returns
	 * it as an instance of {@link Function}, supplying {@code env} as the chunk's sole
	 * upvalue.
	 *
	 * <p>The argument {@code chunkName}, typically the file name of the file containing
	 * {@code chunk}, is used to provide debugging information.</p>
	 *
	 * <p>If {@code chunk} is not a valid Lua program, a {@link LoaderException} is thrown.</p>
	 *
	 * @param env  the variable to be used as the sole upvalue of {@code chunk},
	 *             must not be {@code null}
	 * @param chunkName  chunk name, must not be {@code null}
	 * @param chunk  chunk text, must not be {@code null}
	 * @return  a function object
	 *
	 * @throws LoaderException  if {@code chunk} cannot be converted to a Lua function object
	 *
	 * @throws NullPointerException  if {@code env}, {@code chunkName} or {@code chunk}
	 *                               is {@code null}
	 */
	Function loadTextChunk(Variable env, String chunkName, String chunk) throws LoaderException;

	// TODO: binary chunks
//	Function loadBinaryChunk(Variable env, String chunkName, byte[] chunk, int offset, int len) throws LoaderException;

}
