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

import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;

/**
 * The package library provides basic facilities for loading modules in Lua. It exports
 * one function directly in the global environment: {@link #_require() {@code require}}.
 * Everything else is exported in a {@code table} package.
 */
public abstract class ModuleLib extends Lib {

	@Override
	public void installInto(LuaState state, Table env) {
		env.rawset("require", _require());
		
		Table t = state.newTable();
		env.rawset("package", t);

		t.rawset("config", _config());
		t.rawset("cpath", _cpath());
		t.rawset("loaded", _loaded());
		t.rawset("loadlib", _loadlib());
		t.rawset("path", _path());
		t.rawset("preload", _preload());
		t.rawset("searchers", _searchers());
		t.rawset("searchpath", _searchpath());
	}

	/**
	 * {@code require (modname)}
	 *
	 * <p>Loads the given module. The function starts by looking into
	 * the {@link #_loaded() {@code package.loaded}} table to determine
	 * whether {@code modname} is already loaded. If it is, then {@code require} returns
	 * the value stored at {@code package.loaded[modname]}. Otherwise, it tries to find
	 * a loader for the module.</p>
	 *
	 * <p>To find a loader, {@code require} is guided by the {@code package.searchers} sequence.
	 * By changing this sequence, we can change how {@code require} looks for a module.
	 * The following explanation is based on the default configuration
	 * for {@code package.searchers}.</p>
	 *
	 * <p>First {@code require} queries {@code package.preload[modname]}. If it has a value,
	 * this value (which must be a function) is the loader. Otherwise {@code require}
	 * searches for a Lua loader using the path stored in {@code package.path}. If that also fails,
	 * it searches for a C loader using the path stored in {@code package.cpath}.
	 * If that also fails, it tries an all-in-one loader
	 * (see {@link #_searchers() {@code package.searchers}}).</p>
	 *
	 * <p>Once a loader is found, {@code require} calls the loader with two arguments:
	 * {@code modname} and an extra value dependent on how it got the loader. (If the loader came
	 * from a file, this extra value is the file name.) If the loader returns any non-nil value,
	 * {@code require} assigns the returned value to {@code package.loaded[modname]}.
	 * If the loader does not return a non-nil value and has not assigned any value
	 * to {@code package.loaded[modname]}, then {@code require} assigns <b>true</b> to this entry.
	 * In any case, require returns the final value of {@code package.loaded[modname]}.</p>
	 *
	 * <p>If there is any error loading or running the module, or if it cannot find any loader
	 * for the module, then {@code require} raises an error.</p>
	 *
	 * @return the {@code require} function
	 */
	public abstract Function _require();

	 /**
	 * {@code package.config}
	 *
	 * <p>A string describing some compile-time configurations for packages. This string is
	 * a sequence of lines:</p>
	 *
	 * <ul>
	 * <li>The first line is the directory separator string. Default is '{@code \}' for Windows
	 * and '{@code /}' for all other systems.</li>
	 * <li>The second line is the character that separates templates in a path.
	 * Default is '{@code ;}'.</li>
	 * <li>The third line is the string that marks the substitution points in a template.
	 * Default is '{@code ?}'.</li>
	 * <li>The fourth line is a string that, in a path in Windows, is replaced by
	 * the executable's directory. Default is '{@code !}'.</li>
	 * <li>The fifth line is a mark to ignore all text after it when building
	 * the {@code luaopen_} function name. Default is '{@code -}'.</li>
	 * </ul>
	 *
	 * @return the {@code package.config} string
	 */
	public abstract String _config();

	/**
	 * {@code package.cpath}
	 *
	 * <p>The path used by require to search for a C loader.</p>
	 *
	 * <p>Lua initializes the C path {@code package.cpath} in the same way it initializes
	 * the Lua path {@link #_path() {@code package.path}}, using the environment variable
	 * {@code LUA_CPATH_5_3} or the environment variable {@code LUA_CPATH} or a default path
	 * defined in {@code luaconf.h}.</p>
	 *
	 * @return the {@code package.cpath} string
	 */
	public abstract String _cpath();

	/**
	 * {@code package.loaded}
	 *
	 * <p>A table used by {@link #_require() {@code require}} to control which modules
	 * are already loaded. When you require a module {@code modname}
	 * and {@code package.loaded[modname]} is not <b>false</b>, require simply returns the value
	 * stored there.</p>
	 *
	 * <p>This variable is only a reference to the real table; assignments to this variable do not
	 * change the table used by {@link #_require() {@code require}}.</p>
	 *
	 * @return the {@code package.loaded} table
	 */
	public abstract Table _loaded();

	/**
	 * {@code package.loadlib (libname, funcname)}
	 *
	 * <p>Dynamically links the host program with the C library {@code libname}.</p>
	 *
	 * <p>If funcname is "{@code *}", then it only links with the library, making the symbols
	 * exported by the library available to other dynamically linked libraries. Otherwise,
	 * it looks for a function {@code funcname} inside the library and returns this function as
	 * a C function. So, {@code funcname} must follow the {@code lua_CFunction prototype}
	 * (see {@code lua_CFunction}).</p>
	 *
	 * <p>This is a low-level function. It completely bypasses the package and module system.
	 * Unlike {@link #_require() {@code require}}, it does not perform any path searching
	 * and does not automatically adds extensions. {@code libname} must be the complete file name
	 * of the C library, including if necessary a path and an extension. {@code funcname} must
	 * be the exact name exported by the C library (which may depend on the C compiler and linker
	 * used).</p>
	 *
	 * <p>This function is not supported by Standard C. As such, it is only available on some
	 * platforms (Windows, Linux, Mac OS X, Solaris, BSD, plus other Unix systems that support
	 * the {@code dlfcn} standard).</p>
	 *
	 * @return the {@code package.loadlib} function
	 */
	public abstract Function _loadlib();

	/**
	 * {@code package.path}
	 *
	 * <p>The path used by {@link #_require() {@code require}} to search for a Lua loader.</p>
	 *
	 * <p>At start-up, Lua initializes this variable with the value of the environment variable
	 * {@code LUA_PATH_5_3} or the environment variable {@code LUA_PATH} or with a default path
	 * defined in {@code luaconf.h}, if those environment variables are not defined.
	 * Any "{@code ;;}" in the value of the environment variable is replaced by the default
	 * path.</p>
	 *
	 * @return the {@code package.path} string
	 */
	public abstract String _path();

	/**
	 * {@code package.preload}
	 *
	 * <p>A table to store loaders for specific modules (see
	 * {@link #_require() {@code require}}).</p>
	 *
	 * <p>This variable is only a reference to the real table; assignments to this variable
	 * do not change the table used by {@link #_require() {@code require}}.</p>
	 *
	 * @return the {@code package.preload} table
	 */
	public abstract Table _preload();

	/**
	 * {@code package.searchers}
	 *
	 * <p>A table used by {@link #_require() {@code require}} to control how to load
	 * modules.</p>
	 *
	 * <p>Each entry in this table is a searcher function. When looking for a module,
	 * {@code require} calls each of these searchers in ascending order, with the module name
	 * (the argument given to {@code require}) as its sole parameter. The function can return
	 * another function (the module loader) plus an extra value that will be passed to that loader,
	 * or a string explaining why it did not find that module (or <b>nil</b> if it has nothing
	 * to say).</p>
	 *
	 * <p>Lua initializes this table with four searcher functions.</p>
	 *
	 * <p>The first searcher simply looks for a loader in
	 * the {@link #_preload() {@code package.preload}} table.</p>
	 *
	 * <p>The second searcher looks for a loader as a Lua library, using the path stored at
	 * {@link #_path() {@code package.path}}. The search is done as described in function
	 * {@link #_searchpath() {@code package.searchpath}}.</p>
	 *
	 * <p>The third searcher looks for a loader as a C library, using the path given by
	 * the variable {@link #_cpath() {@code package.cpath}}. Again, the search is done
	 * as described in function {@link #_searchpath() {@code package.searchpath}}.
	 * For instance, if the C path is the string</p>
	 * <pre>
	 *   "./?.so;./?.dll;/usr/local/?/init.so"
	 * </pre>
	 * <p>the searcher for module foo will try to open the files {@code ./foo.so}, {@code ./foo.dll},
	 * and {@code /usr/local/foo/init.so}, in that order. Once it finds a C library,
	 * this searcher first uses a dynamic link facility to link the application with the library.
	 * Then it tries to find a C function inside the library to be used as the loader.
	 * The name of this C function is the string "{@code luaopen_}" concatenated with a copy of
	 * the module name where each dot is replaced by an underscore. Moreover, if the module name
	 * has a hyphen, its suffix after (and including) the first hyphen is removed.
	 * For instance, if the module name is {@code a.b.c-v2.1}, the function name will
	 * be {@code luaopen_a_b_c}.</p>
	 *
	 * <p>The fourth searcher tries an all-in-one loader. It searches the C path for a library
	 * for the root name of the given module. For instance, when requiring {@code a.b.c},
	 * it will search for a C library for {@code a}. If found, it looks into it for
	 * an open function for the submodule; in our example, that would be {@code luaopen_a_b_c}.
	 * With this facility, a package can pack several C submodules into one single library,
	 * with each submodule keeping its original open function.</p>
	 *
	 * <p>All searchers except the first one (preload) return as the extra value the file name
	 * where the module was found, as returned
	 * by {@link #_searchpath() {@code package.searchpath}}. The first searcher returns
	 * no extra value.</p>
	 *
	 * @return the {@code package.searchers} table
	 */
	public abstract Table _searchers();

	/**
	 * {@code package.searchpath (name, path [, sep [, rep]])}
	 *
	 * <p>Searches for the given name in the given {@code path}.</p>
	 *
	 * <p>A path is a string containing a sequence of templates separated by semicolons.
	 * For each template, the function replaces each interrogation mark (if any) in the template
	 * with a copy of name wherein all occurrences of {@code sep} (a dot, by default) were
	 * replaced by {@code rep} (the system's directory separator, by default), and then tries
	 * to open the resulting file name.</p>
	 *
	 * <p>For instance, if the path is the string</p>
	 * <pre>
	 *   "./?.lua;./?.lc;/usr/local/?/init.lua"
	 * </pre>
	 * <p>the search for the name {@code foo.a} will try to open the files {@code ./foo/a.lua},
	 * {@code ./foo/a.lc}, and {@code /usr/local/foo/a/init.lua}, in that order.</p>
	 *
	 * <p>Returns the resulting name of the first file that it can open in read mode
	 * (after closing the file), or <b>nil</b> plus an error message if none succeeds.
	 * (This error message lists all file names it tried to open.)</p>
	 *
	 * @return the {@code package.searchpath} function
	 */
	public abstract Function _searchpath();

}
