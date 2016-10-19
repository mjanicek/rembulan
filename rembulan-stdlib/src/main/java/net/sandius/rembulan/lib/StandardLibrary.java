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

package net.sandius.rembulan.lib;

import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.load.ChunkLoader;

import java.util.Objects;

/**
 * The configuration of the Lua standard library.
 *
 * <p>This is an immutable class that provides transformation methods for manipulating
 * the configuration, and the {@link #installInto(StateContext)} method for installing
 * the standard library with the specified configuration into a Lua state.</p>
 */
public class StandardLibrary {

	private final RuntimeEnvironment environment;

	private final ChunkLoader loader;
	private final boolean withDebug;

	private StandardLibrary(RuntimeEnvironment environment,
							ChunkLoader loader, boolean withDebug) {

		this.environment = Objects.requireNonNull(environment);
		this.loader = loader;
		this.withDebug = withDebug;
	}

	private StandardLibrary(RuntimeEnvironment environment) {
		this(environment, null, false);
	}

	/**
	 * Returns a default configuration for the specified environment.
	 * The default configuration does not include the Debug library and has no chunk loader.
	 *
	 * <p>If any of the standard streams defined by the runtime environment is {@code null},
	 * the corresponding file in the I/O library (such as {@code io.stdin}) will be undefined.
	 * Additionally, if {@code out} is {@code null}, then the global function {@code print}
	 * will be undefined.</p>
	 *
	 * @param environment  the runtime environment, must not be {@code null}
	 * @return  the default configuration
	 *
	 * @throws NullPointerException  if {@code environment} is {@code null}
	 */
	public static StandardLibrary in(RuntimeEnvironment environment) {
		return new StandardLibrary(environment);
	}

	/**
	 * Returns a configuration that differs from this configuration in that
	 * it uses the chunk loader {@code loader}. If {@code loader} is {@code null}, no
	 * chunk loader is used.
	 *
	 * @param loader  the chunk loader, may be {@code null}
	 * @return  a configuration that uses {@code loader} as its chunk loader
	 */
	public StandardLibrary withLoader(ChunkLoader loader) {
		return new StandardLibrary(environment, loader, withDebug);
	}

	/**
	 * Returns a configuration that includes the Debug library iff {@code hasDebug}
	 * is {@code true}.
	 *
	 * @param hasDebug  boolean flag indicating whether to include the Debug library
	 * @return  a configuration that includes the Debug library iff {@code hasDebug} is
	 *          {@code true}
	 */
	public StandardLibrary withDebug(boolean hasDebug) {
		return this.withDebug != hasDebug
				? new StandardLibrary(environment, loader, hasDebug)
				: this;
	}

	/**
	 * Installs the standard library into {@code state}, returning a new table suitable
	 * for use as the global upvalue.
	 *
	 * <p>The returned table is instantiated using the table factory provided by {@code state}.</p>
	 *
	 * @param state  the Lua state context to install into, must not be {@code null}
	 * @return  a new table containing the standard library
	 *
	 * @throws NullPointerException  if {@code state is null}
	 * @throws IllegalStateException  if the configuration is invalid
	 */
	public Table installInto(StateContext state) {
		Objects.requireNonNull(state);
		Table env = state.newTable();

		BasicLib.installInto(state, env, environment, loader);
		ModuleLib.installInto(state, env, environment, loader, ClassLoader.getSystemClassLoader());
		CoroutineLib.installInto(state, env);
		StringLib.installInto(state, env);
		MathLib.installInto(state, env);
		TableLib.installInto(state, env);
		IoLib.installInto(state, env, environment);
		OsLib.installInto(state, env, environment);
		Utf8Lib.installInto(state, env);
		if (withDebug) {
			DebugLib.installInto(state, env);
		}

		return env;
	}

}
