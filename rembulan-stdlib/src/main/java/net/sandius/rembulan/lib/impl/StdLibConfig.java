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

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.Table;
import net.sandius.rembulan.lib.LibContext;
import net.sandius.rembulan.lib.ModuleLib;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.runtime.DefaultLuaState;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Objects;

/**
 * The configuration of the Lua standard library.
 *
 * <p>This is an immutable class that provides transformation methods for manipulating
 * the configuration, and the {@link #installInto(DefaultLuaState)} method for installing
 * the standard library with the specified configuration into a Lua state.</p>
 */
public class StdLibConfig {

	private final InputStream in;
	private final OutputStream out;
	private final OutputStream err;
	private final FileSystem fileSystem;

	private final ChunkLoader loader;

	private final boolean withDebug;

	private StdLibConfig(
			InputStream in, OutputStream out, OutputStream err, FileSystem fileSystem,
			ChunkLoader loader, boolean withDebug) {

		this.in = in;
		this.out = out;
		this.err = err;
		this.fileSystem = fileSystem;
		this.loader = loader;
		this.withDebug = withDebug;
	}

	private StdLibConfig() {
		this(System.in, System.out, System.err, FileSystems.getDefault(), null, false);
	}

	/**
	 * Returns the default configuration. The default configuration does not include the
	 * Debug library, its I/O library operates on the system's defaults, and it has no
	 * chunk loader.
	 *
	 * @return  the default configuration
	 */
	public static StdLibConfig getDefault() {
		return new StdLibConfig();
	}

	/**
	 * Returns a configuration that differs from this configuration in that
	 * it uses the chunk loader {@code loader}. If {@code loader} is {@code null}, no
	 * chunk loader is used.
	 *
	 * @param loader  the chunk loader, may be {@code null}
	 * @return  a configuration that uses {@code loader} as its chunk loader
	 */
	public StdLibConfig withLoader(ChunkLoader loader) {
		return new StdLibConfig(in, out, err, fileSystem, loader, withDebug);
	}

	/**
	 * Returns a configuration that differs from this configuration in that
	 * it uses the specified I/O streams in the I/O library. If any of the streams
	 * is {@code null}, the corresponding file in the I/O library (such as {@code io.stdin})
	 * will be undefined. Additionally, if {@code out} is {@code null}, then the
	 * global function {@code print} will be undefined.
	 *
	 * @param in  the standard input stream, may be {@code null}
	 * @param out  the standard output stream, may be {@code null}
	 * @param err  the standard error stream, may be {@code null}
	 * @return  a configuration that uses the specified streams for its I/O
	 */
	public StdLibConfig withIoStreams(InputStream in, OutputStream out, OutputStream err) {
		return new StdLibConfig(in, out, err, fileSystem, loader, withDebug);
	}

	/**
	 * Returns a configuration that includes the Debug library iff {@code withDebug}
	 * is {@code true}.
	 *
	 * @param withDebug  boolean flag indicating whether to include the Debug library
	 * @return  a configuration that includes the Debug library iff {@code withDebug} is
	 *          {@code true}
	 */
	public StdLibConfig setDebug(boolean withDebug) {
		return this.withDebug != withDebug
				? new StdLibConfig(in, out, err, fileSystem, loader, withDebug)
				: this;
	}

	private Table installInto(LibContext context) {
		Table env = context.newTable();
		new DefaultBasicLib(out != null ? new PrintStream(out) : null, loader, env).installInto(context, env);
		ModuleLib moduleLib = new DefaultModuleLib(context, env);
		moduleLib.installInto(context, env);
		moduleLib.install(new DefaultCoroutineLib());
		moduleLib.install(new DefaultStringLib());
		moduleLib.install(new DefaultMathLib());
		moduleLib.install(new DefaultTableLib());
		moduleLib.install(new DefaultIoLib(context, fileSystem, in, out, err));
		moduleLib.install(new DefaultOsLib());
		moduleLib.install(new DefaultUtf8Lib());
		moduleLib.install(new DefaultDebugLib());
		return env;
	}

	/**
	 * Installs the standard library into {@code state}, returning a new table suitable
	 * for use as the global upvalue.
	 *
	 * @param state  the Lua state to install into, must not be {@code null}
	 * @return  a new table containing the standard library
	 *
	 * @throws NullPointerException  if {@code state is null}
	 * @throws IllegalStateException  if the configuration is invalid
	 */
	public Table installInto(DefaultLuaState state) {
		Objects.requireNonNull(state);
		return installInto(new LibContextImpl(state, state));
	}

}
