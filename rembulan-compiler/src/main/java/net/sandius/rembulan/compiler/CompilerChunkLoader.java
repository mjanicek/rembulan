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

import net.sandius.rembulan.Variable;
import net.sandius.rembulan.load.ChunkClassLoader;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.parser.ParseException;
import net.sandius.rembulan.parser.Parser;
import net.sandius.rembulan.parser.TokenMgrError;
import net.sandius.rembulan.runtime.LuaFunction;

import java.util.Objects;

/**
 * A chunk loader that uses the {@linkplain LuaCompiler compiler} to convert Lua source
 * text to Java classfiles, and loads these classfiles into the VM using a {@link ClassLoader}.
 */
public class CompilerChunkLoader implements ChunkLoader {

	private final ChunkClassLoader chunkClassLoader;
	private final String rootClassPrefix;
	private final LuaCompiler compiler;

	private int idx;

	CompilerChunkLoader(ClassLoader classLoader, LuaCompiler compiler, String rootClassPrefix) {
		this.chunkClassLoader = new ChunkClassLoader(Objects.requireNonNull(classLoader));
		this.compiler = Objects.requireNonNull(compiler);
		this.rootClassPrefix = Objects.requireNonNull(rootClassPrefix);
		this.idx = 0;
	}

	/**
	 * Returns a new instance of {@code CompilerChunkLoader} that uses the specified
	 * class loader {@code classLoader} to load classes it compiles using {@code compiler},
	 * with every main chunk class having the class name {@code rootClassPrefix} followed
	 * by a monotonically-increasing integer suffix.
	 *
	 * @param classLoader  the class loader used by this chunk loader, must not be {@code null}
	 * @param compiler  the compiler instance used by this chunk loader, must not be {@code null}
	 * @param rootClassPrefix  the class name prefix for compiled classes, must not be {@code null}
	 * @return  a new instance of {@code CompilerChunkLoader}
	 *
	 * @throws NullPointerException  if {@code classLoader}, {@code compiler}
	 *                               or {@code rootClassPrefix} is {@code null}
	 */
	public static CompilerChunkLoader of(ClassLoader classLoader, LuaCompiler compiler, String rootClassPrefix) {
		return new CompilerChunkLoader(classLoader, compiler, rootClassPrefix);
	}

	/**
	 * Returns a new instance of {@code CompilerChunkLoader} that uses the specified
	 * class loader {@code classLoader} to load classes it compiles using a new instance
	 * of the Lua compiler with the settings {@code compilerSettings},
	 * with every main chunk class having the class name {@code rootClassPrefix} followed
	 * by a monotonically-increasing integer suffix.
	 *
	 * @param classLoader  the class loader used by this chunk loader, must not be {@code null}
	 * @param compilerSettings  the compiler settings used to instantiate the compiler,
	 *                          must not be {@code null}
	 * @param rootClassPrefix  the class name prefix for compiled classes, must not be {@code null}
	 * @return  a new instance of {@code CompilerChunkLoader}
	 *
	 * @throws NullPointerException  if {@code classLoader}, {@code compilerSettings}
	 *                               or {@code rootClassPrefix} is {@code null}
	 */
	public static CompilerChunkLoader of(ClassLoader classLoader, CompilerSettings compilerSettings, String rootClassPrefix) {
		return new CompilerChunkLoader(classLoader, new LuaCompiler(compilerSettings), rootClassPrefix);
	}

	/**
	 * Returns a new instance of {@code CompilerChunkLoader} that uses the specified
	 * class loader {@code classLoader} to load classes it compiles using a compiler
	 * instantiated with {@linkplain CompilerSettings#defaultSettings() default settings},
	 * with every main chunk class having the class name {@code rootClassPrefix} followed
	 * by a monotonically-increasing integer suffix.
	 *
	 * @param classLoader  the class loader used by this chunk loader, must not be {@code null}
	 * @param rootClassPrefix  the class name prefix for compiled classes, must not be {@code null}
	 * @return  a new instance of {@code CompilerChunkLoader}
	 *
	 * @throws NullPointerException  if {@code classLoader} or {@code rootClassPrefix}
	 *                               is {@code null}
	 */
	public static CompilerChunkLoader of(ClassLoader classLoader, String rootClassPrefix) {
		return of(classLoader, CompilerSettings.defaultSettings(), rootClassPrefix);
	}

	/**
	 * Returns a new instance of {@code CompilerChunkLoader} that uses the class loader
	 * that loaded the {@code CompilerChunkLoader} class to load classes it compiles using
	 * {@code compiler}, with every main chunk class having the class name {@code rootClassPrefix}
	 * followed by a monotonically-increasing integer suffix.
	 *
	 * @param compiler  the compiler instance used by this chunk loader, must not be {@code null}
	 * @param rootClassPrefix  the class name prefix for compiled classes, must not be {@code null}
	 * @return  a new instance of {@code CompilerChunkLoader}
	 *
	 * @throws NullPointerException  {@code compiler} or {@code rootClassPrefix} is {@code null}
	 */
	public static CompilerChunkLoader of(LuaCompiler compiler, String rootClassPrefix) {
		return of(CompilerChunkLoader.class.getClassLoader(), compiler, rootClassPrefix);
	}

	/**
	 * Returns a new instance of {@code CompilerChunkLoader} that uses the class loader
	 * that loaded the {@code CompilerChunkLoader} class to load classes it compiles using
	 * a new instance of the Lua compiler with the settings {@code compilerSettings},
	 * with every main chunk class having the class name {@code rootClassPrefix} followed
	 * by a monotonically-increasing integer suffix.
	 *
	 * @param compilerSettings  the compiler settings used to instantiate the compiler,
	 *                          must not be {@code null}
	 * @param rootClassPrefix  the class name prefix for compiled classes, must not be {@code null}
	 * @return  a new instance of {@code CompilerChunkLoader}
	 *
	 * @throws NullPointerException  if {@code compilerSettings} or {@code rootClassPrefix}
	 *                               is {@code null}
	 */
	public static CompilerChunkLoader of(CompilerSettings compilerSettings, String rootClassPrefix) {
		return of(new LuaCompiler(compilerSettings), rootClassPrefix);
	}

	/**
	 * Returns a new instance of {@code CompilerChunkLoader} that uses the class loader
	 * that loaded the {@code CompilerChunkLoader} class to load classes it compiles using
	 * a compiler instantiated with
	 * {@linkplain CompilerSettings#defaultSettings() default settings},
	 * with every main chunk class having the class name {@code rootClassPrefix} followed
	 * by a monotonically-increasing integer suffix.
	 *
	 * @param rootClassPrefix  the class name prefix for compiled classes, must not be {@code null}
	 * @return  a new instance of {@code CompilerChunkLoader}
	 *
	 * @throws NullPointerException  if {@code rootClassPrefix} is {@code null}
	 */
	public static CompilerChunkLoader of(String rootClassPrefix) {
		return of(CompilerSettings.defaultSettings(), rootClassPrefix);
	}

	public ChunkClassLoader getChunkClassLoader() {
		return chunkClassLoader;
	}

	@Override
	public LuaFunction loadTextChunk(Variable env, String chunkName, String sourceText) throws LoaderException {
		Objects.requireNonNull(env);
		Objects.requireNonNull(chunkName);
		Objects.requireNonNull(sourceText);

		synchronized (this) {
			String rootClassName = rootClassPrefix + (idx++);
			try {
				CompiledModule result = compiler.compile(sourceText, chunkName, rootClassName);

				String mainClassName = chunkClassLoader.install(result);
				Class<?> clazz = chunkClassLoader.loadClass(mainClassName);

				return (LuaFunction) clazz.getConstructor(Variable.class).newInstance(env);
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
	}

//	@Override
//	public LuaFunction loadBinaryChunk(Variable env, String chunkName, byte[] bytes, int offset, int len) throws LoaderException {
//		throw new UnsupportedOperationException();  // TODO
//	}

}
