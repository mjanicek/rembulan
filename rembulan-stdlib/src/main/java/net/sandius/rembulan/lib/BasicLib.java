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
 *
 * --
 * Portions of this file are licensed under the Lua license. For Lua
 * licensing details, please visit
 *
 *     http://www.lua.org/license.html
 *
 * Copyright (C) 1994-2016 Lua.org, PUC-Rio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.sandius.rembulan.lib;

import net.sandius.rembulan.*;
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.runtime.Dispatch;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.IllegalOperationAttemptException;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ProtectedResumable;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.ReturnBuffer;
import net.sandius.rembulan.runtime.UnresolvedControlThrowable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * The basic library provides core functions to Lua. If you do not include this library in your
 * application, you should check carefully whether you need to provide implementations for some
 * of its facilities.
 */
public final class BasicLib {

	/**
	 * The metatable key {@code "__metatable"}. When defined, customises the behaviour
	 * of {@link #getmetatable() <code>getmetatable</code>}
	 * and {@link #setmetatable() <code>setmetatable</code>}.
	 */
	public static final ByteString MT_METATABLE = ByteString.constOf("__metatable");

	/**
	 * The metatable key {@code "__name"}. Used to customise the type name of an object
	 * in error messages.
	 *
	 * @see NameMetamethodValueTypeNamer
	 */
	public static final ByteString MT_NAME = ByteString.constOf("__name");

	/**
	 * The metatable key {@code "__pairs"}. When defined, customises the behaviour
	 * of {@link #pairs() <code>pairs</code>}.
	 */
	public static final ByteString MT_PAIRS = ByteString.constOf("__pairs");

	/**
	 * The metatable key {@code "__tostring"}. When defined, customises the behaviour
	 * of {@link #tostring() <code>tostring</code>}.
	 */
	public static final ByteString MT_TOSTRING = ByteString.constOf("__tostring");

	/**
	 * The name used for describing light userdata in error messages.
	 *
	 * @see NameMetamethodValueTypeNamer
	 */
	public static final ByteString TYPENAME_LIGHT_USERDATA = ByteString.constOf("light userdata");


	static final LuaFunction ASSERT = new Assert();
	static final LuaFunction COLLECTGARBAGE = new CollectGarbage();
	static final LuaFunction ERROR = new Error();
	static final LuaFunction GETMETATABLE = new GetMetatable();
	static final LuaFunction IPAIRS = new IPairs();
	static final LuaFunction NEXT = new Next();
	static final LuaFunction PAIRS = new Pairs();
	static final LuaFunction PCALL = new PCall();
	static final LuaFunction RAWEQUAL = new RawEqual();
	static final LuaFunction RAWGET = new RawGet();
	static final LuaFunction RAWLEN = new RawLen();
	static final LuaFunction RAWSET = new RawSet();
	static final LuaFunction SELECT = new Select();
	static final LuaFunction SETMETATABLE = new SetMetatable();
	static final LuaFunction TONUMBER = new ToNumber();
	static final LuaFunction TOSTRING = new ToString();
	static final LuaFunction TYPE = new Type();
	static final LuaFunction XPCALL = new XPCall();


	/**
	 * Returns the {@code assert} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code assert (v [, message])}
	 *
	 * <p>Calls {@link #error() <code>error</code>} if the value of its argument {@code v}
	 * is false (i.e., <b>nil</b> or <b>false</b>); otherwise, returns all its arguments.
	 * In case of error, {@code message} is the error object; when absent, it defaults
	 * to {@code "assertion failed!"}</p>
	 * </blockquote>
	 *
	 * @return  the {@code assert} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-assert">
	 *     the Lua 5.3 Reference Manual entry for <code>assert</code></a>
	 */
	public static LuaFunction assertFn() {
		return ASSERT;
	}

	/**
	 * Returns the {@code collectgarbage} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code collectgarbage ([opt [, arg]])}
	 *
	 * <p>This function is a generic interface to the garbage collector. It performs different
	 * functions according to its first argument, {@code opt}:</p>
	 * <ul>
	 *   <li><b>{@code "collect"}</b>:
	 *     performs a full garbage-collection cycle. This is the default option.</li>
	 *   <li><b>{@code "stop"}</b>:
	 *     stops automatic execution of the garbage collector. The collector will run only when
	 *     explicitly invoked, until a call to restart it.</li>
	 *   <li><b>{@code "restart"}</b>:
	 *     restarts automatic execution of the garbage collector.</li>
	 *   <li><b>{@code "count"}</b>:
	 *     returns the total memory in use by Lua in Kbytes. The value has a fractional part,
	 *     so that it multiplied by 1024 gives the exact number of bytes in use by Lua
	 *     (except for overflows).</li>
	 *   <li><b>{@code "step"}</b>: performs a garbage-collection step. The step "size"
	 *     is controlled by {@code arg}. With a zero value, the collector will perform
	 *     one basic (indivisible) step. For non-zero values, the collector will perform
	 *     as if that amount of memory (in KBytes) had been allocated by Lua.
	 *     Returns <b>true</b> if the step finished a collection cycle.</li>
	 *   <li><b>{@code "setpause"}</b>:
	 *     sets {@code arg} as the new value for the <i>pause</i> of the collector
	 *     (see §2.5 of the Lua Reference Manual). Returns the previous value
	 *     for <i>pause</i>.</li>
	 *   <li><b>{@code "setstepmul"}</b>:
	 *     sets {@code arg} as the new value for the <i>step multiplier</i>
	 *     of the collector (see §2.5 of the Lua Reference Manual). Returns the previous value
	 *     for <i>step</i>.</li>
	 *   <li><b>{@code "isrunning"}</b>:
	 *     returns a boolean that tells whether the collector is running
	 *     (i.e., not stopped).</li>
	 * </ul>
	 * </blockquote>
	 *
	 * @return  the {@code collectgarbage} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-collectgarbage">
	 *     the Lua 5.3 Reference Manual entry for <code>collectgarbage</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction collectgarbage() {
		return COLLECTGARBAGE;
	}

	/**
	 * Returns a {@code dofile} function that uses the specified chunk loader {@code loader}
	 * and {@code env} as the default global environment for loaded chunks, and opens files
	 * in the specified {@code fileSystem}.
	 *
	 * <p><b>Note:</b> the {@code loadfile} function returned by this method does not
	 * support loading from standard input.</p>
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code dofile ([filename])}
	 *
	 * <p>Opens the named file and executes its contents as a Lua chunk. When called without
	 * arguments, {@code dofile} executes the contents of the standard input
	 * ({@code stdin}). Returns all values returned by the chunk. In case of errors,
	 * {@code dofile} propagates the error to its caller (that is, {@code dofile}
	 * does not run in protected mode).</p>
	 * </blockquote>
	 *
	 * @param env  the default global environment for loaded chunks, may be {@code null}
	 * @param loader  the chunk loader to use, must not be {@code null}
	 * @param fileSystem  the file system to use, must not be {@code null}
	 * @return  the {@code dofile} function
	 *
	 * @throws NullPointerException  if {@code fileSystem} or {@code loader} is {@code null}
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-dofile">
	 *     the Lua 5.3 Reference Manual entry for <code>dofile</code></a>
	 */
	public static LuaFunction dofile(Object env, ChunkLoader loader, FileSystem fileSystem) {
		return new DoFile(fileSystem, loader, env);
	}

	/**
	 * Returns the {@code error} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code error (message [, level])}
	 *
	 * <p>Terminates the last protected function called and returns {@code message}
	 * as the error object. Function {@code error} never returns.</p>
	 *
	 * <p>Usually, {@code error} adds some information about the error position
	 * at the beginning of the message, if the message is a string. The {@code level}
	 * argument specifies how to get the error position. With level 1 (the default),
	 * the error position is where the {@code error} function was called. Level 2 points
	 * the error to where the function that called {@code error} was called; and so on.
	 * Passing a level 0 avoids the addition of error position information to the message.</p>
	 * </blockquote>
	 *
	 * @return  the {@code error} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-error">
	 *     the Lua 5.3 Reference Manual entry for <code>error</code></a>
	 */
	public static LuaFunction error() {
		return ERROR;
	}

	/**
	 * Returns the {@code getmetatable} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code getmetatable (object)}
	 *
	 * <p>If {@code object} does not have a metatable, returns <b>nil</b>. Otherwise,
	 * if the object's metatable has a {@link #MT_METATABLE <code>"__metatable"</code>} field,
	 * returns the associated value. Otherwise, returns the metatable of the given object.</p>
	 * </blockquote>
	 *
	 * @return  the {@code getmetatable} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-getmetatable">
	 *     the Lua 5.3 Reference Manual entry for <code>getmetatable</code></a>
	 */
	public static LuaFunction getmetatable() {
		return GETMETATABLE;
	}

	/**
	 * Returns the {@code ipairs} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code ipairs (t)}
	 *
	 * <p>Returns three values (an iterator function, the table {@code t}, and 0) so that
	 * the construction</p>
	 *
	 *   <blockquote>{@code for i,v in ipairs(t) do body end}</blockquote>
	 *
	 * <p>will iterate over the key–value pairs {@code (1,t[1])}, {@code (2,t[2])}, ...,
	 * up to the first <b>nil</b> value.</p>
	 * </blockquote>
	 *
	 * @return  the {@code ipairs} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-ipairs">
	 *     the Lua 5.3 Reference Manual entry for <code>ipairs</code></a>
	 */
	public static LuaFunction ipairs() {
		return IPAIRS;
	}

	/**
	 * Returns a {@code load} function that uses the specified chunk loader {@code loader}
	 * and {@code env} as the default global environment for loaded chunks.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code load (chunk [, chunkname [, mode [, env]]])}
	 *
	 * <p>Loads a chunk.</p>
	 *
	 * <p>If chunk is a string, the chunk is this string. If {@code chunk} is a function,
	 * {@code load} calls it repeatedly to get the chunk pieces. Each call to chunk must
	 * return a string that concatenates with previous results. A return of an empty string,
	 * <b>nil</b>, or no value signals the end of the chunk.</p>
	 *
	 * <p>If there are no syntactic errors, returns the compiled chunk as a function; otherwise,
	 * returns <b>nil</b> plus the error message.</p>
	 *
	 * <p>If the resulting function has upvalues, the first upvalue is set to the value
	 * of {@code env}, if that parameter is given, or to the value of the global environment.
	 * Other upvalues are initialized with <b>nil</b>. (When you load a main chunk, the resulting
	 * function will always have exactly one upvalue, the {@code _ENV} variable
	 * (see §2.2 of the Lua Reference Manual). However, when you load a binary chunk created
	 * from a function (see {@link StringLib#dump() <code>string.dump</code>}), the resulting
	 * function can have an arbitrary number of upvalues.) All upvalues are fresh, that is,
	 * they are not shared with any other function.</p>
	 *
	 * <p>{@code chunkname} is used as the name of the chunk for error messages and debug
	 * information (see §4.9 of the Lua Reference Manual). When absent, it defaults
	 * to {@code chunk}, if chunk is a string, or to {@code "=(load)"} otherwise.</p>
	 *
	 * <p>The string {@code mode} controls whether the chunk can be text or binary
	 * (that is, a precompiled chunk). It may be the string {@code "b"} (only binary chunks),
	 * {@code "t"} (only text chunks), or {@code "bt"} (both binary and text).
	 * The default is {@code "bt"}.</p>
	 *
	 * <p>Lua does not check the consistency of binary chunks. Maliciously crafted binary
	 * chunks can crash the interpreter.</p>
	 * </blockquote>
	 *
	 * @param env  the default global environment for loaded chunks, may be {@code null}
	 * @param loader  the chunk loader to use, must not be {@code null}
	 * @return  the {@code load} function
	 *
	 * @throws NullPointerException  if {@code loader} is {@code null}
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-load">
	 *     the Lua 5.3 Reference Manual entry for <code>load</code></a>
	 */
	public static LuaFunction load(Object env, ChunkLoader loader) {
		return new Load(loader, env);
	}

	/**
	 * Returns a {@code loadfile} function that uses the specified chunk loader {@code loader}
	 * and {@code env} as the default global environment for loaded chunks, and opens files
	 * in the specified {@code fileSystem}.
	 *
	 * <p><b>Note:</b> the {@code loadfile} function returned by this method does not
	 * support loading from standard input.</p>
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code loadfile ([filename [, mode [, env]]])}
	 *
	 * <p>Similar to {@link Load <code>load</code>}, but gets the chunk from file
	 * {@code filename} or from the standard input, if no file name is given.</p>
	 * </blockquote>
	 *
	 * @param env  the default global environment for loaded chunks, may be {@code null}
	 * @param loader  the chunk loader to use, must not be {@code null}
	 * @param fileSystem  the file system to use, must not be {@code null}
	 * @return  the {@code loadfile} function
	 *
	 * @throws NullPointerException  if {@code fileSystem} or {@code loader} is {@code null}
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-loadfile">
	 *     the Lua 5.3 Reference Manual entry for <code>loadfile</code></a>
	 */
	public static LuaFunction loadfile(Object env, ChunkLoader loader, FileSystem fileSystem) {
		return new LoadFile(fileSystem, loader, env);
	}

	/**
	 * Returns the {@code next} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code next (table [, index])}
	 *
	 * <p>Allows a program to traverse all fields of a table. Its first argument is a table
	 * and its second argument is an index in this table. {@code next} returns the next
	 * index of the table and its associated value. When called with <b>nil</b> as its second
	 * argument, {@code next} returns an initial index and its associated value.
	 * When called with the last index, or with <b>nil</b> in an empty table, next returns
	 * <b>nil</b>. If the second argument is absent, then it is interpreted as <b>nil</b>.
	 * In particular, you can use {@code next(t)} to check whether a table is empty.</p>
	 *
	 * <p>The order in which the indices are enumerated is not specified, even for numeric
	 * indices. (To traverse a table in numerical order, use a numerical <b>for</b>.)</p>
	 *
	 * <p>The behavior of {@code next} is undefined if, during the traversal, you assign
	 * any value to a non-existent field in the table. You may however modify existing fields.
	 * In particular, you may clear existing fields.</p>
	 * </blockquote>
	 *
	 * @return  the {@code next} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-next">
	 *     the Lua 5.3 Reference Manual entry for <code>next</code></a>
	 */
	public static LuaFunction next() {
		return NEXT;
	}

	/**
	 * Returns the {@code pairs} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code pairs (t)}
	 *
	 * <p>If {@code t} has a metamethod {@link #MT_PAIRS <code>"__pairs"</code>}, calls it with
	 * {@code t} as argument and returns the first three results from the call.</p>
	 *
	 * <p>Otherwise, returns three values: the {@link #next() <code>next</code>} function,
	 * the table {@code t}, and <b>nil</b>, so that the construction</p>
	 *
	 *   <blockquote>{@code for k,v in pairs(t) do body end}</blockquote>
	 *
	 * <p>will iterate over all key–value pairs of table {@code t}.</p>
	 *
	 * <p>See function {@link #next() <code>next</code>} for the caveats of modifying the table
	 * during its traversal.</p>
	 * </blockquote>
	 *
	 * @return  the {@code pairs} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-pairs">
	 *     the Lua 5.3 Reference Manual entry for <code>pairs</code></a>
	 */
	public static LuaFunction pairs() {
		return PAIRS;
	}

	/**
	 * Returns the {@code pcall} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code pcall (f [, arg1, ···])}
	 *
	 * <p>Calls function {@code f} with the given arguments in protected mode. This means
	 * that any error inside {@code f} is not propagated; instead, {@code pcall}
	 * catches the error and returns a status code. Its first result is the status code
	 * (a boolean), which is <b>true</b> if the call succeeds without errors. In such case,
	 * {@code pcall} also returns all results from the call, after this first result.
	 * In case of any error, {@code pcall} returns <b>false</b> plus the error message.</p>
	 * </blockquote>
	 *
	 * @return  the {@code pcall} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-pcall">
	 *     the Lua 5.3 Reference Manual entry for <code>pcall</code></a>
	 */
	public static LuaFunction pcall() {
		return PCALL;
	}

	/**
	 * Returns a {@code print} function that writes its output to the output stream {@code out}
	 * and looks up the {@code tostring} function in the supplied environment {@code env}.
	 *
	 * <p><b>Note:</b> the function returned by this method looks up {@code tostring} by
	 * evaluating the Lua expression {@code env["tostring"]} every time it is invoked. It does
	 * <i>not</i> call {@link #tostring()} directly. This is the (undocumented) behaviour
	 * of the {@code print} function provided by PUC-Lua 5.3.3.</p>
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code print (···)}
	 *
	 * <p>Receives any number of arguments and prints their values to {@code stdout},
	 * using the {@link #tostring() <code>tostring</code>} function to convert each argument
	 * to a string. {@code print} is not intended for formatted output, but only as
	 * a quick way to show a value, for instance for debugging. For complete control over
	 * the output, use {@link StringLib#format() <code>string.format</code>}
	 * and {@code io.write}.</p>
	 * </blockquote>
	 *
	 * @param out  the output stream, must not be {@code null}
	 * @param env  the environment for looking up the global function {@code tostring},
	 *             may be {@code null}
	 * @return  the {@code print} function
	 *
	 * @throws NullPointerException  if {@code out} is {@code null}
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-print">
	 *     the Lua 5.3 Reference Manual entry for <code>print</code></a>
	 */
	public static LuaFunction print(OutputStream out, Object env) {
		return new Print(out, env);
	}

	/**
	 * Returns the {@code rawequal} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code rawequal (v1, v2)}
	 *
	 * <p>Checks whether {@code v1} is equal to {@code v2}, without invoking any
	 * metamethod. Returns a boolean.</p>
	 * </blockquote>
	 *
	 * @return  the {@code rawequal} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-rawequal">
	 *     the Lua 5.3 Reference Manual entry for <code>rawequal</code></a>
	 */
	public static LuaFunction rawequal() {
		return RAWEQUAL;
	}

	/**
	 * Returns the {@code rawget} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code rawget (table, index)}
	 *
	 * <p>Gets the real value of {@code table[index]}, without invoking any metamethod.
	 * {@code table} must be a table; {@code index} may be any value.</p>
	 * </blockquote>
	 *
	 * @return  the {@code rawget} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-rawget">
	 *     the Lua 5.3 Reference Manual entry for <code>rawget</code></a>
	 */
	public static LuaFunction rawget() {
		return RAWGET;
	}

	/**
	 * Returns the {@code rawlen} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code rawlen (v)}
	 *
	 * <p>Returns the length of the object {@code v}, which must be a table or a string,
	 * without invoking any metamethod. Returns an integer.</p>
	 * </blockquote>
	 *
	 * @return  the {@code rawlen} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-rawlen">
	 *     the Lua 5.3 Reference Manual entry for <code>rawlen</code></a>
	 */
	public static LuaFunction rawlen() {
		return RAWLEN;
	}

	/**
	 * Returns the {@code rawset} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code rawset (table, index, value)}
	 *
	 * <p>Sets the real value of {@code table[index]} to {@code value}, without
	 * invoking any metamethod. {@code table} must be a table, {@code index} any value
	 * different from <b>nil</b> and NaN, and {@code value} any Lua value.</p>
	 *
	 * <p>This function returns {@code table}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code rawset} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-rawset">
	 *     the Lua 5.3 Reference Manual entry for <code>rawset</code></a>
	 */
	public static LuaFunction rawset() {
		return RAWSET;
	}

	/**
	 * Returns the {@code select} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code select (index, ···)}
	 *
	 * <p>If {@code index} is a number, returns all arguments after argument number index;
	 * a negative number indexes from the end (-1 is the last argument). Otherwise, index must
	 * be the string {@code "#"}, and select returns the total number of extra arguments
	 * it received.</p>
	 * </blockquote>
	 *
	 * @return  the {@code select} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-select">
	 *     the Lua 5.3 Reference Manual entry for <code>select</code></a>
	 */
	public static LuaFunction select() {
		return SELECT;
	}

	/**
	 * Returns the {@code setmetatable} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code setmetatable (table, metatable)}
	 *
	 * <p>Sets the metatable for the given {@code table}. (To change the metatable of other
	 * types from Lua code, you must use the debug library (see §6.10 of the Lua Reference
	 * Manual).) If {@code metatable} is <b>nil</b>, removes the metatable of the given
	 * table. If the original metatable has a {@link #MT_METATABLE <code>"__metatable"</code>}
	 * field, raises an error.</p>
	 *
	 * <p>This function returns {@code table}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code setmetatable} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-setmetatable">
	 *     the Lua 5.3 Reference Manual entry for <code>setmetatable</code></a>
	 */
	public static LuaFunction setmetatable() {
		return SETMETATABLE;
	}

	/**
	 * Returns the {@code tonumber} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code tonumber (e [, base])}
	 *
	 * <p>When called with no {@code base}, {@code tonumber} tries to convert
	 * its argument to a number. If the argument is already a number or a string convertible to
	 * a number (see §3.4.2 of the Lua Reference Manual), then {@code tonumber} returns
	 * this number; otherwise, it returns <b>nil</b>.</p>
	 *
	 * <p>When called with {@code base}, then {@code e} should be a string to be
	 * interpreted as an integer numeral in that base. The base may be any integer between
	 * 2 and 36, inclusive. In bases above 10, the letter 'A' (in either upper or lower case)
	 * represents 10, 'B' represents 11, and so forth, with 'Z' representing 35. If the string
	 * {@code e} is not a valid numeral in the given base, the function returns
	 * <b>nil</b>.</p>
	 * </blockquote>
	 *
	 * @return  the {@code tonumber} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-tonumber">
	 *     the Lua 5.3 Reference Manual entry for <code>tonumber</code></a>
	 */
	public static LuaFunction tonumber() {
		return TONUMBER;
	}

	/**
	 * Returns the {@code tostring} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code tostring (v)}
	 *
	 * <p>Receives a value of any type and converts it to a string in a human-readable format.
	 * (For complete control of how numbers are converted,
	 * use {@link StringLib#format() <code>string.format</code>}.) If the metatable
	 * of {@code v} has a {@link #MT_TOSTRING <code>"__tostring"</code>} field,
	 * then {@code tostring} calls the corresponding value with {@code v} as argument, and uses
	 * the result of the call as its result.</p>
	 * </blockquote>
	 *
	 * @return  the {@code tostring} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-tostring">
	 *     the Lua 5.3 Reference Manual entry for <code>tostring</code></a>
	 */
	public static LuaFunction tostring() {
		return TOSTRING;
	}

	/**
	 * Returns the {@code type} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code type (v)}
	 *
	 * <p>Returns the type of its only argument, coded as a string. The possible results of this
	 * function are {@code "nil"} (a string, not the value <b>nil</b>),
	 * {@code "number"}, {@code "string"}, {@code "boolean"},
	 * {@code "table"}, {@code "function"}, {@code "thread"},
	 * and {@code "userdata"}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code type} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-type">
	 *     the Lua 5.3 Reference Manual entry for <code>type</code></a>
	 */
	public static LuaFunction type() {
		return TYPE;
	}

	/**
	 * The global variable {@code _VERSION}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code _VERSION}
	 *
	 * <p>A global variable (not a function) that holds a string containing the running Lua
	 * version. The current value of this variable (in PUC-Lua 5.3.x)
	 * is {@code "Lua 5.3"}.</p>
	 * </blockquote>
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-_VERSION">
	 *     the Lua 5.3 Reference Manual entry for <code>_VERSION</code></a>
	 */
	public static final ByteString _VERSION = ByteString.constOf("Lua 5.3");

	/**
	 * Returns the {@code xpcall} function.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code xpcall (f, msgh [, arg1, ···])}
	 *
	 * <p>This function is similar to {@link #pcall() <code>pcall</code>}, except that it sets
	 * a new message handler {@code msgh}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code xpcall} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-xpcall">
	 *     the Lua 5.3 Reference Manual entry for <code>xpcall</code></a>
	 */
	public static LuaFunction xpcall() {
		return XPCALL;
	}


	private BasicLib() {
		// not to be instantiated
	}

	/**
	 * Installs the basic library into the specified table {@code env} in the state context
	 * {@code context}. Uses {@code runtimeEnvironment} for I/O facilities and {@code loader}
	 * for chunk loading.
	 *
	 * <p>The following functions are only installed if the conditions are satisfied:</p>
	 * <ul>
	 *     <li>{@code dofile}: if {@code runtimeEnvironment != null
	 *       && runtimeEnvironment.fileSystem() != null && loader != null};</li>
	 *     <li>{@code load}: if {@code loader != null};</li>
	 *     <li>{@code loadfile}: if {@code runtimeEnvironment != null
	 *       && runtimeEnvironment.fileSystem() != null && loader != null};</li>
	 *     <li>{@code print}: if {@code runtimeEnvironment != null
	 *       && runtimeEnvironment.standardOutput() != null};</li>
	 * </ul>
	 *
	 * @param context  the state context, must not be {@code null}
	 * @param env  the global environment, must not be {@code null}
	 * @param runtimeEnvironment  the runtime environment to use, may be {@code null}
	 * @param loader  the chunk loader to use, may be {@code null}
	 *
	 * @throws NullPointerException  if {@code context} or {@code env} is {@code null}
	 */
	public static void installInto(StateContext context, Table env, RuntimeEnvironment runtimeEnvironment, ChunkLoader loader) {
		Objects.requireNonNull(context);  // not needed, but included for consistency
		Objects.requireNonNull(env);

		OutputStream out = runtimeEnvironment != null ? runtimeEnvironment.standardOutput() : null;
		FileSystem fileSystem = runtimeEnvironment != null ? runtimeEnvironment.fileSystem() : null;

		env.rawset("assert", assertFn());
		env.rawset("collectgarbage", collectgarbage());
		if (loader != null && fileSystem != null) env.rawset("dofile", dofile(env, loader, fileSystem));
		env.rawset("error", error());
		env.rawset("_G", env);
		env.rawset("getmetatable", getmetatable());
		env.rawset("ipairs", ipairs());
		if (loader != null) env.rawset("load", load(env, loader));
		if (loader != null && fileSystem != null) env.rawset("loadfile", loadfile(env, loader, fileSystem));
		env.rawset("next", next());
		env.rawset("pairs", pairs());
		env.rawset("pcall", pcall());
		if (out != null) env.rawset("print", print(out, env));
		env.rawset("rawequal", rawequal());
		env.rawset("rawget", rawget());
		env.rawset("rawlen", rawlen());
		env.rawset("rawset", rawset());
		env.rawset("select", select());
		env.rawset("setmetatable", setmetatable());
		env.rawset("tostring", tostring());
		env.rawset("tonumber", tonumber());
		env.rawset("type", type());
		env.rawset("_VERSION", _VERSION);
		env.rawset("xpcall", xpcall());

		ModuleLib.addToLoaded(env, "_G", env);
	}

	static class Print extends AbstractLibFunction {

		static class SuspendedState {

			private final int state;
			private final Object tostring;
			private final Object[] args;

			SuspendedState(int state, Object tostring, Object[] args) {
				this.state = state;
				this.tostring = tostring;
				this.args = args;
			}

		}

		private final OutputStream out;
		private final Object envTable;

		public Print(OutputStream out, Object envTable) {
			this.out = Objects.requireNonNull(out);
			this.envTable = Objects.requireNonNull(envTable);
		}

		@Override
		protected String name() {
			return "print";
		}

		private void run(ExecutionContext context, int state, Object tostring, Object[] args) throws ResolvedControlThrowable {

			switch (state) {

				case 0:
					try {
						Dispatch.index(context, envTable, ToString.KEY);
					}
					catch (UnresolvedControlThrowable ct) {
						throw ct.resolve(this, new SuspendedState(1, null, args));
					}

				case 1:
					tostring = context.getReturnBuffer().get0();

				case 2:
					try {
						for (int i = 0; i < args.length; i++) {
							Object a = args[i];
							try {
								Dispatch.call(context, tostring, a);
							}
							catch (UnresolvedControlThrowable ct) {
								throw ct.resolve(this, new SuspendedState(2, tostring, Arrays.copyOfRange(args, i + 1, args.length)));
							}

							Object s = context.getReturnBuffer().get0();
							s = Conversions.canonicalRepresentationOf(s);

							if (s instanceof ByteString) {
								((ByteString) s).writeTo(out);
							}
							else {
								throw new LuaRuntimeException("error calling 'print' ('tostring' must return a string to 'print')");
							}

							if (i + 1 < args.length) {
								out.write((byte) '\t');
							}
						}
						out.write((byte) '\n');
						out.flush();
					}
					catch (IOException ex) {
						throw new LuaRuntimeException(ex);
					}

					// returning nothing
					context.getReturnBuffer().setTo();
					return;

				default: throw new IllegalStateException("Illegal state: " + state);
			}
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			run(context, 0, null, args.copyAll());
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			SuspendedState ss = (SuspendedState) suspendedState;
			run(context, ss.state, ss.tostring, ss.args);
		}

	}

	static class Type extends AbstractLibFunction {

		@Override
		protected String name() {
			return "type";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(args.nextAny());
			context.getReturnBuffer().setTo(typeName);
		}

	}

	static class Next extends AbstractLibFunction {

		@Override
		protected String name() {
			return "next";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table table = args.nextTable();
			Object index = args.nextOptionalAny(null);

			final Object nxt;

			if (index != null) {
				nxt = table.successorKeyOf(index);
			}
			else {
				nxt = table.initialKey();
			}

			if (nxt == null) {
				// we've reached the end
				context.getReturnBuffer().setTo(null);
			}
			else {
				Object value = table.rawget(nxt);
				context.getReturnBuffer().setTo(nxt, value);
			}
		}

	}

	static class INext extends AbstractLibFunction {

		public static final INext INSTANCE = new INext();

		@Override
		protected String name() {
			return "inext";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args)
				throws ResolvedControlThrowable {

			Table table = args.nextTable();
			long index = args.nextInteger();

			index += 1;

			try {
				Dispatch.index(context, table, index);
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, index);
			}

			Object result = context.getReturnBuffer().get0();
			processResult(context, index, result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			long index = (Long) suspendedState;
			Object result = context.getReturnBuffer().get0();
			processResult(context, index, result);
		}

		private static void processResult(ExecutionContext context, long index, Object o) throws ResolvedControlThrowable {
			if (o != null) {
				context.getReturnBuffer().setTo(index, o);
			}
			else {
				context.getReturnBuffer().setTo(null);
			}
		}

	}

	static class Pairs extends AbstractLibFunction {

		@Override
		protected String name() {
			return "pairs";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table t = args.nextTable();
			Object metamethod = Metatables.getMetamethod(context, MT_PAIRS, t);

			if (metamethod != null) {
				try {
					Dispatch.call(context, metamethod, t);
				}
				catch (UnresolvedControlThrowable ct) {
					throw ct.resolve(this, null);
				}

				ReturnBuffer rbuf = context.getReturnBuffer();
				rbuf.setTo(rbuf.get0(), rbuf.get1(), rbuf.get2());
			}
			else {
				ReturnBuffer rbuf = context.getReturnBuffer();
				rbuf.setTo(NEXT, t, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			ReturnBuffer rbuf = context.getReturnBuffer();
			rbuf.setTo(rbuf.get0(), rbuf.get1(), rbuf.get2());
		}

	}

	static class IPairs extends AbstractLibFunction {

		@Override
		protected String name() {
			return "ipairs";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table t = args.nextTable();
			context.getReturnBuffer().setTo(INext.INSTANCE, t, 0L);
		}

	}

	static class ToString extends AbstractLibFunction {

		static final ByteString KEY = ByteString.constOf("tostring");

		@Override
		protected String name() {
			return "tostring";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object arg = args.nextAny();

			Object meta = Metatables.getMetamethod(context, MT_TOSTRING, arg);
			if (meta != null) {
				try {
					Dispatch.call(context, meta, arg);
				}
				catch (UnresolvedControlThrowable ct) {
					throw ct.resolve(this, null);
				}

				// resume
				resume(context, null);
			}
			else {
				// no metamethod, just call the default toString
				ByteString s = Conversions.toHumanReadableString(arg);
				context.getReturnBuffer().setTo(s);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// trim to single value
			Object result = context.getReturnBuffer().get0();
			context.getReturnBuffer().setTo(result);
		}

	}

	static class ToNumber extends AbstractLibFunction {

		public static Long toNumber(ByteString s, int base) {
			try {
				return Long.parseLong(s.toString().trim(), base);
			}
			catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		protected String name() {
			return "tonumber";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			if (args.size() < 2) {
				// no base
				Object o = args.nextAny();
				Number n = Conversions.numericalValueOf(o);
				context.getReturnBuffer().setTo(n);
			}
			else {
				// We do the argument checking gymnastics in order to achieve the same error
				// reporting as in PUC-Lua. We first check that base (#2) is an integer, then
				// retrieve the string (#1), and then check that the base is within range.

				args.skip();
				args.nextInteger();
				args.rewind();
				ByteString s = args.nextStrictString();
				int base = args.nextIntRange(Character.MIN_RADIX, Character.MAX_RADIX, "base");

				context.getReturnBuffer().setTo(toNumber(s, base));

			}
		}

	}


	static class GetMetatable extends AbstractLibFunction {

		@Override
		protected String name() {
			return "getmetatable";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object arg = args.nextAny();
			Object meta = Metatables.getMetamethod(context, MT_METATABLE, arg);

			Object result = meta != null
					? meta  // __metatable field present, return its value
					: context.getMetatable(arg);  // return the entire metatable

			context.getReturnBuffer().setTo(result);
		}

	}

	static class SetMetatable extends AbstractLibFunction {

		@Override
		protected String name() {
			return "setmetatable";
		}

		private Table nilOrTable(ArgumentIterator args) {
			if (args.hasNext()) {
				Object o = args.peek();
				if (o instanceof Table) return (Table) o;
				else if (o == null) return null;
			}
			throw new BadArgumentException(2, name(), "nil or table expected");
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table t = args.nextTable();
			Table mt = nilOrTable(args);

			if (Metatables.getMetamethod(context, MT_METATABLE, t) != null) {
				throw new IllegalOperationAttemptException("cannot change a protected metatable");
			}
			else {
				t.setMetatable(mt);
				context.getReturnBuffer().setTo(t);
			}
		}

	}

	static class Error extends AbstractLibFunction {

		@Override
		protected String name() {
			return "error";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			// TODO: handle levels
			Object arg1 = args.nextOptionalAny(null);
			throw new LuaRuntimeException(arg1);
		}

	}

	static class Assert extends AbstractLibFunction {

		@Override
		protected String name() {
			return "assert";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			if (Conversions.booleanValueOf(args.nextAny())) {
				context.getReturnBuffer().setToContentsOf(args.copyAll());
			}
			else {
				final AssertionFailedException ex;
				if (args.hasNext()) {
					// message is defined
					Object message = args.nextAny();
					ByteString stringMessage = Conversions.stringValueOf(message);
					if (stringMessage != null) {
						ex = new AssertionFailedException(stringMessage);
					}
					else {
						ex = new AssertionFailedException(message);
					}
				}
				else {
					// message not defined, use the default
					ex = new AssertionFailedException("assertion failed!");
				}

				throw ex;
			}

		}

	}

	static class PCall extends AbstractLibFunction implements ProtectedResumable {

		@Override
		protected String name() {
			return "pcall";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object callTarget = args.nextAny();
			Object[] callArgs = args.copyRemaining();

			try {
				Dispatch.call(context, callTarget, callArgs);
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, null);
			}
			catch (Exception ex) {
				resumeError(context, null, Conversions.toErrorObject(ex));
				return;
			}

			resume(context, null);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// success: prepend true
			ReturnBuffer rbuf = context.getReturnBuffer();
			ArrayList<Object> result = new ArrayList<>();
			result.add(Boolean.TRUE);
			result.addAll(Arrays.asList(rbuf.getAsArray()));
			rbuf.setToContentsOf(result);
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ResolvedControlThrowable {
			context.getReturnBuffer().setTo(Boolean.FALSE, error);  // failure
		}

	}

	static class XPCall extends AbstractLibFunction implements ProtectedResumable {

		public static final int MAX_DEPTH = 220;  // 220 in PUC-Lua 5.3

		@Override
		protected String name() {
			return "xpcall";
		}

		private static class SavedState {
			public final LuaFunction handler;
			public final int depth;

			private SavedState(LuaFunction handler, int depth) {
				this.handler = handler;
				this.depth = depth;
			}
		}

		private static void prependTrue(ExecutionContext context) {
			ReturnBuffer rbuf = context.getReturnBuffer();
			ArrayList<Object> result = new ArrayList<>();
			result.add(Boolean.TRUE);
			result.addAll(Arrays.asList(rbuf.getAsArray()));
			rbuf.setToContentsOf(result);
		}

		private static void prependFalseAndTrim(ExecutionContext context) {
			ReturnBuffer rbuf = context.getReturnBuffer();
			Object errorObject = rbuf.get0();
			rbuf.setTo(Boolean.FALSE, errorObject);
		}

		private void handleError(ExecutionContext context, LuaFunction handler, int depth, Object errorObject) throws ResolvedControlThrowable {
			// we want to be able to handle nil error objects, so we need a separate flag
			boolean isError = true;

			while (isError && depth < MAX_DEPTH) {
				depth += 1;

				try {
					Dispatch.call(context, handler, errorObject);
					isError = false;
				}
				catch (UnresolvedControlThrowable ct) {
					throw ct.resolve(this, new SavedState(handler, depth));
				}
				catch (Exception e) {
					errorObject = Conversions.toErrorObject(e);
					isError = true;
				}
			}

			if (!isError) {
				prependFalseAndTrim(context);
			}
			else {
				// depth must be >= MAX_DEPTH
				context.getReturnBuffer().setTo(Boolean.FALSE, "error in error handling");
			}
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object callTarget = args.hasNext() ? args.peek() : null;
			args.skip();
			LuaFunction handler = args.nextFunction();
			Object[] callArgs = args.copyRemaining();

			Object errorObject = null;
			boolean isError = false;  // need to distinguish nil error objects from no-error

			try {
				Dispatch.call(context, callTarget, callArgs);
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, new SavedState(handler, 0));
			}
			catch (Exception e) {
				errorObject = Conversions.toErrorObject(e);
				isError = true;
			}

			if (!isError) {
				prependTrue(context);
			}
			else {
				handleError(context, handler, 0, errorObject);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			SavedState ss = (SavedState) suspendedState;
			if (ss.depth == 0) {
				prependTrue(context);
			}
			else {
				prependFalseAndTrim(context);
			}
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ResolvedControlThrowable {
			SavedState ss = (SavedState) suspendedState;
			handleError(context, ss.handler, ss.depth, error);
		}

	}

	static class RawEqual extends AbstractLibFunction {

		@Override
		protected String name() {
			return "rawequal";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object a = args.nextAny();
			Object b = args.nextAny();
			context.getReturnBuffer().setTo(Ordering.isRawEqual(a, b));
		}

	}

	static class RawGet extends AbstractLibFunction {

		@Override
		protected String name() {
			return "rawget";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table table = args.nextTable();
			Object key = args.nextAny();
			context.getReturnBuffer().setTo(table.rawget(key));
		}

	}

	static class RawSet extends AbstractLibFunction {

		@Override
		protected String name() {
			return "rawset";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table table = args.nextTable();
			Object key = args.nextAny();
			Object value = args.nextAny();

			table.rawset(key, value);
			context.getReturnBuffer().setTo(table);
		}

	}

	static class RawLen extends AbstractLibFunction {

		@Override
		protected String name() {
			return "rawlen";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			final long result;

			// no need to distinguish missing value vs nil
			Object arg1 = args.nextOptionalAny(null);

			if (arg1 instanceof Table) {
				Table table = (Table) arg1;
				result = table.rawlen();
			}
			else if (arg1 instanceof ByteString) {
				ByteString s = (ByteString) arg1;
				result = (long) s.length();
			}
			else if (arg1 instanceof String) {
				String s = (String) arg1;
				result = Dispatch.len(s);
			}
			else {
				throw new BadArgumentException(1, name(), "table or string expected");
			}

			context.getReturnBuffer().setTo(result);
		}

	}

	static class Select extends AbstractLibFunction {

		@Override
		protected String name() {
			return "select";
		}

		private static boolean isHash(Object o) {
			if (o instanceof ByteString) return ((ByteString) o).startsWith((byte) '#');
			else if (o instanceof String) return ((String) o).startsWith("#");
			else return false;
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object index = args.hasNext() ? args.peek() : null;

			if (isHash(index)) {
				// return the number of remaining args
				int count = args.size() - 1;
				context.getReturnBuffer().setTo((long) count);
			}
			else {
				int idx = args.nextIntRange(-args.size() + 1, Integer.MAX_VALUE, "index");

				int from = idx >= 0
						? idx  // from the beginning
						: args.size() + idx;  // idx < 0: from the end (-1 is the last index)

				if (from < 1) {
					throw new BadArgumentException(1, name(), "index out of range");
				}

				Object[] r = args.copyAll();
				final Object[] result;
				result = from > r.length ? new Object[0] : Arrays.copyOfRange(r, from, r.length);
				context.getReturnBuffer().setToContentsOf(result);
			}
		}

	}

	static class CollectGarbage extends AbstractLibFunction {

		@Override
		protected String name() {
			return "collectgarbage";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			if (args.hasNext()) {
				throw new UnsupportedOperationException();  // TODO
			}
			// TODO
		}

	}

	static class Load extends AbstractLibFunction {

		static final ByteString DEFAULT_MODE = ByteString.constOf("bt");

		private final ChunkLoader loader;
		private final Object defaultEnv;

		public Load(ChunkLoader loader, Object env) {
			this.loader = Objects.requireNonNull(loader);
			this.defaultEnv = env;
		}

		@Override
		protected String name() {
			return "load";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {

			// chunk
			final Object chunk = args.hasNext() && Conversions.stringValueOf(args.peek()) != null
					? args.nextString()
					: args.nextFunction();

			assert (chunk != null);

			// chunk name
			final String chunkName;
			if (args.hasNext() && args.peek() != null) {
				chunkName = "[string \"" + args.nextString() + "\"]";
			}
			else {
				if (args.hasNext()) args.skip();  // next is nil
				chunkName = chunk instanceof ByteString
						? "[string \"" + chunk + "\"]"
						: "=(load)";
			}

			final ByteString modeString = args.nextOptionalString(DEFAULT_MODE);
			final Object env = args.nextOptionalAny(defaultEnv);

			// TODO: binary chunks

			if (!modeString.contains((byte) 't')) {
				ByteStringBuilder bld = new ByteStringBuilder();
				bld.append("attempt to load a text chunk (mode is '").append(modeString).append("')");
				context.getReturnBuffer().setTo(null, bld.toByteString());
			}
			else {
				if (chunk instanceof ByteString) {
					loadFromString(context, chunkName, env, (ByteString) chunk);
				}
				else {
					LuaFunction fn = (LuaFunction) chunk;
					loadFromFunction(context, false, chunkName, env, new ByteStringBuilder(), fn);
				}
			}

		}

		private void loadFromString(ExecutionContext context, String chunkName, Object env, ByteString chunkText) {
			final LuaFunction fn;
			try {
				fn = loader.loadTextChunk(new Variable(env), chunkName, chunkText.toString());
			}
			catch (LoaderException ex) {
				context.getReturnBuffer().setTo(null, ex.getLuaStyleErrorMessage());
				return;
			}

			if (fn != null) {
				context.getReturnBuffer().setTo(fn);
			}
			else {
				// don't trust the loader to return a non-null value
				context.getReturnBuffer().setTo(null, "loader returned nil");
			}
		}

		private static class State {

			public final String chunkName;
			public final Object env;
			public final ByteStringBuilder bld;
			public final LuaFunction fn;

			private State(String chunkName, Object env, ByteStringBuilder bld, LuaFunction fn) {
				this.chunkName = chunkName;
				this.env = env;
				this.bld = bld;
				this.fn = fn;
			}

		}

		private void loadFromFunction(ExecutionContext context, boolean resuming, String chunkName, Object env, ByteStringBuilder bld, LuaFunction fn)
				throws ResolvedControlThrowable {

			ByteString chunkText = null;

			try {
				while (chunkText == null) {
					if (!resuming) {
						Dispatch.call(context, fn);
					}

					resuming = false;

					Object o = context.getReturnBuffer().get0();
					if (o == null) {
						chunkText = bld.toByteString();
					}
					else {
						ByteString s = Conversions.stringValueOf(o);
						if (s != null) {
							bld.append(s);
						}
						else {
							context.getReturnBuffer().setTo(null, "reader function must return a string");
							return;
						}
					}
				}
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, new State(chunkName, env, bld, fn));
			}

			assert (chunkText != null);

			loadFromString(context, chunkName, env, chunkText);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			State state = (State) suspendedState;
			loadFromFunction(context, true, state.chunkName, state.env, state.bld, state.fn);
		}

	}

	static class LoadFile extends AbstractLibFunction {

		private final FileSystem fileSystem;
		private final ChunkLoader loader;
		private final Object defaultEnv;

		public LoadFile(FileSystem fileSystem, ChunkLoader loader, Object defaultEnv) {
			this.fileSystem = Objects.requireNonNull(fileSystem);
			this.loader = Objects.requireNonNull(loader);
			this.defaultEnv = defaultEnv;
		}

		@Override
		protected String name() {
			return "loadfile";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {

			final ByteString fileName = args.nextOptionalString(null);
			final ByteString modeString = args.nextOptionalString(Load.DEFAULT_MODE);
			final Object env = args.nextOptionalAny(defaultEnv);

			boolean isStdin = fileName == null;

			// chunk name
			final String chunkName = isStdin ? "stdin" : fileName.toString();

			if (isStdin) {
				context.getReturnBuffer().setTo(null, "not supported: loadfile from stdin");
				return;
			}
			else {
				final LuaFunction fn;
				try {
					fn = loadTextChunkFromFile(fileSystem, loader, chunkName, modeString, env);
				}
				catch (LoaderException ex) {
					context.getReturnBuffer().setTo(null, ex.getLuaStyleErrorMessage());
					return;
				}

				assert (fn != null);

				context.getReturnBuffer().setTo(fn);
			}

		}

	}

	static LuaFunction loadTextChunkFromFile(FileSystem fileSystem, ChunkLoader loader, String fileName, ByteString modeString, Object env)
			throws LoaderException {

		final LuaFunction fn;
		try {
			Path p = fileSystem.getPath(fileName);

			if (!modeString.contains((byte) 't')) {
				throw new LuaRuntimeException("attempt to load a text chunk (mode is '" + modeString + "')");
			}

			// FIXME: this is extremely wasteful!
			byte[] bytes = Files.readAllBytes(p);
			ByteString chunkText = ByteString.copyOf(bytes);
			fn = loader.loadTextChunk(new Variable(env), fileName, chunkText.toString());
		}
		catch (InvalidPathException | IOException ex) {
			throw new LoaderException(ex, fileName);
		}

		if (fn == null) {
			throw new LuaRuntimeException("loader returned nil");
		}

		return fn;
	}

	static class DoFile extends AbstractLibFunction {

		private final FileSystem fileSystem;
		private final ChunkLoader loader;
		private final Object env;

		public DoFile(FileSystem fileSystem, ChunkLoader loader, Object env) {
			this.fileSystem = Objects.requireNonNull(fileSystem);
			this.loader = Objects.requireNonNull(loader);
			this.env = env;
		}

		@Override
		protected String name() {
			return "dofile";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			final ByteString fileName = args.nextOptionalString(null);

			if (fileName == null) {
				throw new UnsupportedOperationException("not supported: 'dofile' from stdin");
			}

			// TODO: we'll only be executing this function once -- add functionality to ChunkLoader to give us a "temporary" loader?

			final LuaFunction fn;
			try {
				fn = loadTextChunkFromFile(fileSystem, loader, fileName.toString(), Load.DEFAULT_MODE, env);
			}
			catch (LoaderException ex) {
				throw new LuaRuntimeException(ex.getLuaStyleErrorMessage());
			}

			try {
				Dispatch.call(context, fn);
			}
			catch (UnresolvedControlThrowable ct) {
				ct.resolve(this, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// no-op: results are already in the result buffer
		}

	}

}
