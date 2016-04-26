package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.util.Check;

public abstract class BasicLib implements Lib {

	@Override
	public void installInto(LuaState state, Table env) {
		Check.notNull(env);

		LibUtils.setIfNonNull(env, "_G", env);
		LibUtils.setIfNonNull(env, "_VERSION", __VERSION());
		
		LibUtils.setIfNonNull(env, "print", _print());
		LibUtils.setIfNonNull(env, "type", _type());

		LibUtils.setIfNonNull(env, "next", _next());
		LibUtils.setIfNonNull(env, "pairs", _pairs());
		LibUtils.setIfNonNull(env, "ipairs", _ipairs());

		LibUtils.setIfNonNull(env, "tostring", _tostring());
		LibUtils.setIfNonNull(env, "tonumber", _tonumber());

		LibUtils.setIfNonNull(env, "error", _error());
		LibUtils.setIfNonNull(env, "assert", _assert());

		LibUtils.setIfNonNull(env, "getmetatable", _getmetatable());
		LibUtils.setIfNonNull(env, "setmetatable", _setmetatable());

		LibUtils.setIfNonNull(env, "pcall", _pcall());
		LibUtils.setIfNonNull(env, "xpcall", _xpcall());

		LibUtils.setIfNonNull(env, "rawequal", _rawequal());
		LibUtils.setIfNonNull(env, "rawget", _rawget());
		LibUtils.setIfNonNull(env, "rawset", _rawset());
		LibUtils.setIfNonNull(env, "rawlen", _rawlen());

		LibUtils.setIfNonNull(env, "select", _select());

		LibUtils.setIfNonNull(env, "collectgarbage", _collectgarbage());
		LibUtils.setIfNonNull(env, "dofile", _dofile());
		LibUtils.setIfNonNull(env, "load", _load());
		LibUtils.setIfNonNull(env, "loadfile", _loadfile());
	}

	/**
	 * {@code _VERSION}
	 *
	 * <p>A global variable (not a function) that holds a string containing the running Lua
	 * version. The current value of this variable (in PUC-Lua 5.3.x)
	 * is {@code "Lua 5.3"}.</p>
	 */
	public abstract String __VERSION();

	/**
	 * print (···)
	 *
	 * <p>Receives any number of arguments and prints their values to {@code stdout},
	 * using the {@link #_tostring() {@code tostring}} function to convert each argument
	 * to a string. {@code print} is not intended for formatted output, but only as
	 * a quick way to show a value, for instance for debugging. For complete control over
	 * the output, use {@link StringLib#_format() {@code string.format}}
	 * and {@link IOLib#_write() {@code io.write}}.</p>
	 */
	public abstract Function _print();

	/**
	 * {@code type (v)}
	 *
	 * <p>Returns the type of its only argument, coded as a string. The possible results of this
	 * function are {@code "nil"} (a string, not the value <b>nil</b>),
	 * {@code "number"}, {@code "string"}, {@code "boolean"},
	 * {@code "table"}, {@code "function"}, {@code "thread"},
	 * and {@code "userdata"}.</p>
	 */
	public abstract Function _type();

	/**
	 * {@code tostring (v)}
	 *
	 * <p>Receives a value of any type and converts it to a string in a human-readable format.
	 * (For complete control of how numbers are converted,
	 * use {@link StringLib#_format() {@code string.format}}.) If the metatable
	 * of {@code v} has a {@code "__tostring"} field, then {@code tostring} calls
	 * the corresponding value with {@code v} as argument, and uses the result
	 * of the call as its result.</p>
	 */
	public abstract Function _tostring();

	/**
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
	 * <b>nil<b>.</p>
	 */
	public abstract Function _tonumber();

	/**
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
	 */
	public abstract Function _error();

	/**
	 * {@code assert (v [, message])}
	 *
	 * <p>Calls {@link #_error() {@code error}} if the value of its argument {@code v}
	 * is false (i.e., <b>nil</b> or <b>false</b>); otherwise, returns all its arguments.
	 * In case of error, {@code message} is the error object; when absent, it defaults
	 * to {@code "assertion failed!"}</p>
	 */
	public abstract Function _assert();


	/**
	 * {@code getmetatable (object)}
	 *
	 * <p>If {@code object} does not have a metatable, returns <b>nil</b>. Otherwise,
	 * if the object's metatable has a {@code "__metatable"} field, returns the associated
	 * value. Otherwise, returns the metatable of the given object.</p>
	 */
	public abstract Function _getmetatable();

	/**
	 * {@code setmetatable (table, metatable)}
	 *
	 * <p>Sets the metatable for the given {@code table}. (To change the metatable of other
	 * types from Lua code, you must use the debug library (see §6.10 of the Lua Reference
	 * Manual).) If {@code metatable} is <b>nil</b>, removes the metatable of the given
	 * table. If the original metatable has a {@code "__metatable"} field, raises
	 * an error.</p>
	 *
	 * <p>This function returns {@code table}.</p>
	 */
	public abstract Function _setmetatable();


	/**
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
	 */
	public abstract Function _next();

	/**
	 * {@code pairs (t)}
	 *
	 * <p>If {@code t} has a metamethod {@code __pairs}, calls it with {@code t}
	 * as argument and returns the first three results from the call.</p>
	 *
	 * <p>Otherwise, returns three values: the {@link #_next() {@code next}} function,
	 * the table {@code t}, and <b>nil</b>, so that the construction
	 *
	 *   <blockquote>{@code for k,v in pairs(t) do body end}</blockquote>
	 *
	 * will iterate over all key–value pairs of table {@code t}.</p>
	 *
	 * <p>See function {@link #_next() {@code next}} for the caveats of modifying the table
	 * during its traversal.</p>
	 */
	public abstract Function _pairs();

	/**
	 * {@code ipairs (t)}
	 *
	 * <p>Returns three values (an iterator function, the table {@code t}, and 0) so that
	 * the construction
	 *
	 *   <blockquote>{@code for i,v in ipairs(t) do body end}</blockquote>
	 *
	 * will iterate over the key–value pairs {@code (1,t[1])}, {@code (2,t[2])}, ...,
	 * up to the first <b>nil</b> value.</p>
	 */
	public abstract Function _ipairs();


	/**
	 * {@code pcall (f [, arg1, ···])}
	 *
	 * <p>Calls function {@code f} with the given arguments in protected mode. This means
	 * that any error inside {@code f} is not propagated; instead, {@code pcall}
	 * catches the error and returns a status code. Its first result is the status code
	 * (a boolean), which is <b>true</b> if the call succeeds without errors. In such case,
	 * {@code pcall} also returns all results from the call, after this first result.
	 * In case of any error, {@code pcall} returns <b>false</b> plus the error message.</p>
	 */
	public abstract Function _pcall();

	/**
	 * {@code xpcall (f, msgh [, arg1, ···])}
	 *
	 * <p>This function is similar to {@link #_pcall() <code>pcall</code>}, except that it sets
	 * a new message handler {@code msgh}.</p>
	 */
	public abstract Function _xpcall();


	/**
	 * {@code rawequal (v1, v2)}
	 *
	 * <p>Checks whether {@code v1} is equal to {@code v2}, without invoking any
	 * metamethod. Returns a boolean.</p>
	 */
	public abstract Function _rawequal();

	/**
	 * {@code rawget (table, index)}
	 *
	 * <p>Gets the real value of {@code table[index]}, without invoking any metamethod.
	 * {@code table} must be a table; {@code index} may be any value.</p>
	 */
	public abstract Function _rawget();

	/**
	 * {@code rawset (table, index, value)}
	 *
	 * <p>Sets the real value of {@code table[index]} to {@code value}, without
	 * invoking any metamethod. {@code table} must be a table, {@code index} any value
	 * different from <b>nil</b> and NaN, and {@code value} any Lua value.</p>
	 *
	 * <p>This function returns {@code table}.</p>
	 */
	public abstract Function _rawset();

	/**
	 * {@code rawlen (v)}
	 *
	 * <p>Returns the length of the object {@code v}, which must be a table or a string,
	 * without invoking any metamethod. Returns an integer.</p>
	 */
	public abstract Function _rawlen();

	/**
	 * {@code select (index, ···)}
	 *
	 * <p>If {@code index} is a number, returns all arguments after argument number index;
	 * a negative number indexes from the end (-1 is the last argument). Otherwise, index must
	 * be the string {@code "#"}, and select returns the total number of extra arguments
	 * it received.</p>
	 */
	public abstract Function _select();

	/**
	 * {@code collectgarbage ([opt [, arg]])}
	 *
	 * <p>This function is a generic interface to the garbage collector. It performs different
	 * functions according to its first argument, {@code opt}:
	 *   <ul>
	 *     <li><b>{@code "collect"}</b>:
	 *       performs a full garbage-collection cycle. This is the default option.</li>
	 *     <li><b>{@code "stop"}</b>:
	 *       stops automatic execution of the garbage collector. The collector will run only when
	 *       explicitly invoked, until a call to restart it.</li>
	 *     <li><b>{@code "restart"}</b>:
	 *       restarts automatic execution of the garbage collector.</li>
	 *     <li><b>{@code "count"}</b>:
	 *       returns the total memory in use by Lua in Kbytes. The value has a fractional part,
	 *       so that it multiplied by 1024 gives the exact number of bytes in use by Lua
	 *       (except for overflows).</li>
	 *     <li><b>{@code "step"}</b>: performs a garbage-collection step. The step "size"
	 *       is controlled by {@code arg}. With a zero value, the collector will perform
	 *       one basic (indivisible) step. For non-zero values, the collector will perform
	 *       as if that amount of memory (in KBytes) had been allocated by Lua.
	 *       Returns <b>true</b> if the step finished a collection cycle.</li>
	 *     <li><b>{@code "setpause"}</b>:
	 *       sets {@code arg} as the new value for the <i>pause</i> of the collector
	 *       (see §2.5 of the Lua Reference Manual). Returns the previous value
	 *       for <i>pause</i>.</li>
	 *     <li><b>{@code "setstepmul"}</b>:
	 *       sets {@code arg} as the new value for the <i>step multiplier</i>
	 *       of the collector (see §2.5 of the Lua Reference Manual). Returns the previous value
	 *       for <i>step</i>.</li>
	 *     <li><b>{@code "isrunning"}</b>:
	 *       returns a boolean that tells whether the collector is running
	 *       (i.e., not stopped).</li>
	 *   </ul>
	 * </p>
	 */
	public abstract Function _collectgarbage();

	/**
	 * {@code dofile ([filename])}
	 *
	 * <p>Opens the named file and executes its contents as a Lua chunk. When called without
	 * arguments, {@code dofile} executes the contents of the standard input
	 * ({@code stdin}). Returns all values returned by the chunk. In case of errors,
	 * {@code dofile} propagates the error to its caller (that is, {@code dofile}
	 * does not run in protected mode).</p>
	 */
	public abstract Function _dofile();

	/**
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
	 * from a function (see {@link StringLib#_dump() {@code string.dump}}), the resulting
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
	 */
	public abstract Function _load();

	/**
	 * {@code loadfile ([filename [, mode [, env]]])}
	 *
	 * <p>Similar to {@link #_load() {@code load}}, but gets the chunk from file
	 * {@code filename} or from the standard input, if no file name is given.</p>
	 */
	public abstract Function _loadfile();
	
}
