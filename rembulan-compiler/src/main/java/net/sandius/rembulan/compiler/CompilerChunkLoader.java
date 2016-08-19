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

import net.sandius.rembulan.core.ChunkLoader;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LoaderException;
import net.sandius.rembulan.core.Variable;
import net.sandius.rembulan.core.load.ChunkClassLoader;
import net.sandius.rembulan.parser.ParseException;
import net.sandius.rembulan.util.Check;

public class CompilerChunkLoader extends ChunkLoader {

	private final ChunkClassLoader chunkClassLoader;
	private final Compiler compiler;

	private int idx;

	public CompilerChunkLoader(ClassLoader classLoader, Compiler compiler) {
		this.chunkClassLoader = new ChunkClassLoader(classLoader);
		this.compiler = Check.notNull(compiler);
		this.idx = 0;
	}

	public CompilerChunkLoader(
			ClassLoader classLoader,
			CompilerSettings compilerSettings) {
		this(classLoader, new Compiler(compilerSettings));
	}

	public CompilerChunkLoader(ClassLoader classLoader) {
		this(classLoader, CompilerSettings.defaultSettings());
	}

	@Override
	public Function loadTextChunk(Variable env, String chunkName, String sourceText) throws LoaderException {
		try {
			CompiledModule result = compiler.compile(sourceText, "stdin", "f" + (idx++));  // FIXME

			String mainClassName = chunkClassLoader.install(result);
			Class<?> clazz = chunkClassLoader.loadClass(mainClassName);

			return (Function) clazz.getConstructor(Variable.class).newInstance(env);
		}
		catch (ParseException | RuntimeException | LinkageError | ReflectiveOperationException ex) {
			throw new LoaderException(ex);
		}
	}

	@Override
	public Function loadBinaryChunk(Variable env, String chunkName, byte[] bytes, int offset, int len) throws LoaderException {
		throw new UnsupportedOperationException();  // TODO
	}

}
