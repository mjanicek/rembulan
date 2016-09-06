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

import net.sandius.rembulan.Function;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.load.ChunkClassLoader;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.parser.ParseException;
import net.sandius.rembulan.parser.Parser;
import net.sandius.rembulan.parser.TokenMgrError;
import net.sandius.rembulan.util.Check;

import java.util.Objects;

public class CompilerChunkLoader implements ChunkLoader {

	private final ChunkClassLoader chunkClassLoader;
	private final String rootClassPrefix;
	private final Compiler compiler;

	private int idx;

	public CompilerChunkLoader(ClassLoader classLoader, Compiler compiler, String rootClassPrefix) {
		this.chunkClassLoader = new ChunkClassLoader(classLoader);
		this.compiler = Check.notNull(compiler);
		this.rootClassPrefix = Check.notNull(rootClassPrefix);
		this.idx = 0;
	}

	public CompilerChunkLoader(
			ClassLoader classLoader,
			CompilerSettings compilerSettings,
			String rootClassPrefix) {
		this(classLoader, new Compiler(compilerSettings), rootClassPrefix);
	}

	public CompilerChunkLoader(ClassLoader classLoader, String rootClassPrefix) {
		this(classLoader, CompilerSettings.defaultSettings(), rootClassPrefix);
	}

	public ChunkClassLoader getChunkClassLoader() {
		return chunkClassLoader;
	}

	@Override
	public Function loadTextChunk(Variable env, String chunkName, String sourceText) throws LoaderException {
		Objects.requireNonNull(env);
		Objects.requireNonNull(chunkName);
		Objects.requireNonNull(sourceText);

		String rootClassName = rootClassPrefix + (idx++);
		try {
			CompiledModule result = compiler.compile(sourceText, chunkName, rootClassName);

			String mainClassName = chunkClassLoader.install(result);
			Class<?> clazz = chunkClassLoader.loadClass(mainClassName);

			return (Function) clazz.getConstructor(Variable.class).newInstance(env);
		}
		catch (TokenMgrError ex) {
			String msg = ex.getMessage();
			int line = 0;  // TODO
			boolean partial = msg != null && msg.contains("Encountered: <EOF>");  // TODO: is there really no better way?
			throw new LoaderException(ex, chunkName, line, partial);
		}
		catch (ParseException ex) {
			boolean partial = ex.currentToken != null
					&& ex.currentToken.next != null
					&& ex.currentToken.next.kind == Parser.EOF;
			int line = ex.currentToken != null
					? ex.currentToken.beginLine
					: 0;
			throw new LoaderException(ex, chunkName, line, partial);
		}
		catch (RuntimeException | LinkageError | ReflectiveOperationException ex) {
			throw new LoaderException(ex, chunkName, 0, false);
		}
	}

//	@Override
//	public Function loadBinaryChunk(Variable env, String chunkName, byte[] bytes, int offset, int len) throws LoaderException {
//		throw new UnsupportedOperationException();  // TODO
//	}

}
