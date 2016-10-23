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

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.ByteStringBuilder;
import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.Metatables;
import net.sandius.rembulan.PlainValueTypeNamer;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.impl.NonsuspendableFunctionException;
import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.runtime.AbstractFunction0;
import net.sandius.rembulan.runtime.Dispatch;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.IllegalOperationAttemptException;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.UnresolvedControlThrowable;
import net.sandius.rembulan.util.ByteIterator;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>This library provides generic functions for string manipulation, such as finding
 * and extracting substrings, and pattern matching. When indexing a string in Lua,
 * the first character is at position 1 (not at 0, as in C). Indices are allowed to be negative
 * and are interpreted as indexing backwards, from the end of the string. Thus, the last character
 * is at position -1, and so on.</p>
 *
 * <p>The string library provides all its functions inside the table {@code string}. It also sets
 * a metatable for strings where the {@code __index} field points to the {@code string} table.
 * Therefore, you can use the string functions in object-oriented style. For instance,
 * {@code string.byte(s,i)} can be written as {@code s:byte(i)}.</p>
 *
 * <p>The string library assumes one-byte character encodings.</p>
 */
public final class StringLib {

	static final LuaFunction BYTE = new Byte();
	static final LuaFunction CHAR = new Char();
	static final LuaFunction DUMP = new Dump();
	static final LuaFunction FIND = new Find();
	static final LuaFunction FORMAT = new Format();
	static final LuaFunction GMATCH = new GMatch();
	static final LuaFunction GSUB = new GSub();
	static final LuaFunction LEN = new Len();
	static final LuaFunction LOWER = new Lower();
	static final LuaFunction MATCH = new Match();
	static final LuaFunction PACK = new Pack();
	static final LuaFunction PACKSIZE = new PackSize();
	static final LuaFunction REP = new Rep();
	static final LuaFunction REVERSE = new Reverse();
	static final LuaFunction SUB = new Sub();
	static final LuaFunction UNPACK = new Unpack();
	static final LuaFunction UPPER = new Upper();


	/**
	 * Returns the function {@code string.byte}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.byte (s [, i [, j]])}
	 *
	 * <p>Returns the internal numeric codes of the characters
	 * {@code s[i]}, {@code s[i+1]}, ..., {@code s[j]}. The default value for {@code i} is 1;
	 * the default value for {@code j} is {@code i}. These indices are corrected following
	 * the same rules of function {@link #SUB {@code string.sub}}.</p>
	 *
	 * <p>Numeric codes are not necessarily portable across platforms.</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.byte} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.byte">
	 *     the Lua 5.3 Reference Manual entry for <code>string.byte</code></a>
	 */
	public static LuaFunction byteFn() {
		return BYTE;
	}

	/**
	 * Returns the function {@code string.char}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.char (···)}
	 *
	 * <p>Receives zero or more integers. Returns a string with length equal to the number
	 * of arguments, in which each character has the internal numeric code equal
	 * to its corresponding argument.</p>
	 *
	 * <p>Numeric codes are not necessarily portable across platforms.</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.char} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.char">
	 *     the Lua 5.3 Reference Manual entry for <code>string.char</code></a>
	 */
	public static LuaFunction charFn() {
		return CHAR;
	}

	/**
	 * Returns the function {@code string.dump}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.dump (function [, strip])}
	 *
	 * <p>Returns a string containing a binary representation (a binary chunk) of the given
	 * function, so that a later {@link BasicLib.Load {@code load}} on this string
	 * returns a copy of the function (but with new upvalues). If {@code strip} is a true value,
	 * the binary representation may not include all debug information about the function,
	 * to save space.</p>
	 *
	 * <p>Functions with upvalues have only their number of upvalues saved. When (re)loaded,
	 * those upvalues receive fresh instances containing <b>nil</b>. (You can use the debug
	 * library to serialize and reload the upvalues of a function in a way adequate
	 * to your needs.)</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.dump} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.dump">
	 *     the Lua 5.3 Reference Manual entry for <code>string.dump</code></a>
	 */
	public static LuaFunction dump() {
		return DUMP;
	}

	/**
	 * Returns the function {@code string.find}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.find (s, pattern [, init [, plain]])}
	 *
	 * <p>Looks for the first match of {@code pattern} (see §6.4.1) in the string {@code s}.
	 * If it finds a match, then find returns the indices of {@code s} where this occurrence
	 * starts and ends; otherwise, it returns <b>nil</b>. A third, optional numeric argument
	 * {@code init} specifies where to start the search; its default value is 1 and can
	 * be negative. A value of <b>true</b> as a fourth, optional argument {@code plain}
	 * turns off the pattern matching facilities, so the function does a plain "find substring"
	 * operation, with no characters in pattern being considered magic. Note that
	 * if {@code plain} is given, then {@code init} must be given as well.</p>
	 *
	 * <p>If the {@code pattern} has captures, then in a successful match the captured values
	 * are also returned, after the two indices.</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.find} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.find">
	 *     the Lua 5.3 Reference Manual entry for <code>string.find</code></a>
	 */
	public static LuaFunction find() {
		return FIND;
	}

	/**
	 * Returns the function {@code string.format}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.format (formatstring, ···)}
	 *
	 * <p>Returns a formatted version of its variable number of arguments following
	 * the description given in its first argument (which must be a string). The format string
	 * follows the same rules as the ISO C function {@code sprintf}. The only differences
	 * are that the options/modifiers {@code *}, {@code h}, {@code L}, {@code l}, {@code n},
	 * and {@code p} are not supported and that there is an extra option, {@code q}.
	 * The {@code q} option formats a string between double quotes, using escape sequences
	 * when necessary to ensure that it can safely be read back by the Lua interpreter.
	 * For instance, the call</p>
	 *
	 * <pre>
	 * {@code
	 * string.format('%q', 'a string with "quotes" and \n new line')
	 * }
	 * </pre>
	 *
	 * <p>may produce the string:</p>
	 *
	 * <pre>
	 * {@code
	 * "a string with \"quotes\" and \
	 * new line"}
	 * }
	 * </pre>
	 *
	 * <p>Options {@code A}, {@code a}, {@code E}, {@code e}, {@code f}, {@code G}, and {@code g}
	 * all expect a number as argument. Options {@code c}, {@code d}, {@code i}, {@code o},
	 * {@code u}, {@code X}, and {@code x} expect an integer. Option {@code q} expects a string.
	 * Option {@code s} expects a string; if its argument is not a string, it is converted
	 * to one following the same rules of {@link BasicLib#TOSTRING {@code tostring}}.
	 * If the option has any modifier (flags, width, length), the string argument should
	 * not contain embedded zeros. When Lua is compiled with a non-C99 compiler, options
	 * {@code A} and {@code a} (hexadecimal floats) do not support any modifier (flags, width,
	 * length).</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.format} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.format">
	 *     the Lua 5.3 Reference Manual entry for <code>string.format</code></a>
	 */
	public static LuaFunction format() {
		return FORMAT;
	}

	/**
	 * Returns the function {@code string.gmatch}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.gmatch (s, pattern)}
	 *
	 * <p>Returns an iterator function that, each time it is called, returns the next captures
	 * from {@code pattern} (see §6.4.1) over the string {@code s}. If {@code pattern} specifies
	 * no captures, then the whole match is produced in each call.</p>
	 *
	 * <p>As an example, the following loop will iterate over all the words from string {@code s},
	 * printing one per line:
	 *
	 * <pre>
	 * {@code
	 * s = "hello world from Lua"
	 * for w in string.gmatch(s, "%a+") do
	 *   print(w)
	 * end
	 * }
	 * </pre>
	 *
	 * <p>The next example collects all pairs {@code key=value} from the given string into
	 * a table:</p>
	 *
	 * <pre>
	 * {@code
	 * t = {}
	 * s = "from=world, to=Lua"
	 * for k, v in string.gmatch(s, "(%w+)=(%w+)") do
	 *   t[k] = v
	 * end
	 * }
	 * </pre>
	 *
	 * <p>For this function, a caret '{@code ^}' at the start of a pattern does not work as
	 * an anchor, as this would prevent the iteration.</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.gmatch} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.gmatch">
	 *     the Lua 5.3 Reference Manual entry for <code>string.gmatch</code></a>
	 */
	public static LuaFunction gmatch() {
		return GMATCH;
	}

	/**
	 * Returns the function {@code string.gsub}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.gsub (s, pattern, repl [, n])}
	 *
	 * <p>Returns a copy of {@code s} in which all (or the first {@code n}, if given) occurrences
	 * of the pattern (see §6.4.1) have been replaced by a replacement string specified
	 * by {@code repl}, which can be a string, a table, or a function. {@code gsub} also returns,
	 * as its second value, the total number of matches that occurred. The name {@code gsub} comes
	 * from <i>Global SUBstitution</i>.</p>
	 *
	 * <p>If {@code repl} is a string, then its value is used for replacement.
	 * The character {@code %} works as an escape character: any sequence in {@code repl}
	 * of the form {@code %d}, with {@code d} between 1 and 9, stands for the value of
	 * the {@code d}-th captured substring. The sequence {@code %0} stands for the whole match.
	 * The sequence {@code %%} stands for a single {@code %}.</p>
	 *
	 * <p>If {@code repl} is a table, then the table is queried for every match, using the first
	 * capture as the key.</p>
	 *
	 * <p>If {@code repl} is a function, then this function is called every time a match occurs,
	 * with all captured substrings passed as arguments, in order.</p>
	 *
	 * <p>In any case, if the pattern specifies no captures, then it behaves as if the whole
	 * pattern was inside a capture.</p>
	 *
	 * <p>If the value returned by the table query or by the function call is a string
	 * or a number, then it is used as the replacement string; otherwise, if it is <b>false</b>
	 * or <b>nil</b>, then there is no replacement (that is, the original match is kept
	 * in the string).</p>
	 *
	 * <p>Here are some examples:</p>
	 *
	 * <pre>
	 * {@code
	 * x = string.gsub("hello world", "(%w+)", "%1 %1")
	 * --> x="hello hello world world"
	 * }
	 * </pre>
	 *
	 * <pre>
	 * {@code
	 * x = string.gsub("hello world", "%w+", "%0 %0", 1)
	 * --> x="hello hello world"
	 * }
	 * </pre>
	 *
	 * <pre>
	 * {@code
	 * x = string.gsub("hello world from Lua", "(%w+)%s*(%w+)", "%2 %1")
	 * --> x="world hello Lua from"
	 * }
	 * </pre>
	 *
	 * <pre>
	 * {@code
	 * x = string.gsub("home = $HOME, user = $USER", "%$(%w+)", os.getenv)
	 * --> x="home = /home/roberto, user = roberto"
	 * }
	 * </pre>
	 *
	 * <pre>
	 * {@code
	 * x = string.gsub("4+5 = $return 4+5$", "%$(.-)%$", function (s)
	 *       return load(s)()
	 *     end)
	 * --> x="4+5 = 9"
	 * }
	 * </pre>
	 *
	 * <pre>
	 * {@code
	 * local t = {name="lua", version="5.3"}
	 * x = string.gsub("$name-$version.tar.gz", "%$(%w+)", t)
	 * --> x="lua-5.3.tar.gz"
	 * }
	 * </pre>
	 * </blockquote>
	 *
	 * @return  the {@code string.gsub} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.gsub">
	 *     the Lua 5.3 Reference Manual entry for <code>string.gsub</code></a>
	 */
	public static LuaFunction gsub() {
		return GSUB;
	}

	/**
	 * Returns the function {@code string.len}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.len (s)}
	 *
	 * <p>Receives a string and returns its length. The empty string {@code ""} has length 0.
	 * Embedded zeros are counted, so {@code "a\000bc\000"} has length 5.</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.len} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.len">
	 *     the Lua 5.3 Reference Manual entry for <code>string.len</code></a>
	 */
	public static LuaFunction len() {
		return LEN;
	}

	/**
	 * Returns the function {@code string.lower}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.lower (s)}
	 *
	 * <p>Receives a string and returns a copy of this string with all uppercase letters changed
	 * to lowercase. All other characters are left unchanged. The definition of what an uppercase
	 * letter is depends on the current locale.</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.lower} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.lower">
	 *     the Lua 5.3 Reference Manual entry for <code>string.lower</code></a>
	 */
	public static LuaFunction lower() {
		return LOWER;
	}

	/**
	 * Returns the function {@code string.match}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.match (s, pattern [, init])}
	 *
	 * <p>Looks for the first match of {@code pattern} (see §6.4.1) in the string {@code s}.
	 * If it finds one, then {@code match} returns the captures from the pattern; otherwise
	 * it returns <b>nil</b>. If {@code pattern} specifies no captures, then the whole match
	 * is returned. A third, optional numeric argument {@code init} specifies where to start
	 * the search; its default value is 1 and can be negative.</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.match} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.match">
	 *     the Lua 5.3 Reference Manual entry for <code>string.match</code></a>
	 */
	public static LuaFunction match() {
		return MATCH;
	}

	/**
	 * Returns the function {@code string.pack}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.pack (fmt, v1, v2, ···)}
	 *
	 * <p>Returns a binary string containing the values {@code v1}, {@code v2}, etc. packed
	 * (that is, serialized in binary form) according to the format string {@code fmt}
	 * (see §6.4.2).</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.pack} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.pack">
	 *     the Lua 5.3 Reference Manual entry for <code>string.pack</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction pack() {
		return PACK;
	}

	/**
	 * Returns the function {@code string.packsize}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.packsize (fmt)}
	 *
	 * <p>Returns the size of a string resulting from {@link #PACK {@code string.pack}}
	 * with the given format. The format string cannot have the variable-length options
	 * '{@code s}' or '{@code z}' (see §6.4.2).</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.packsize} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.packsize">
	 *     the Lua 5.3 Reference Manual entry for <code>string.packsize</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction packsize() {
		return PACKSIZE;
	}

	/**
	 * Returns the function {@code string.rep}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.rep (s, n [, sep])}
	 *
	 * <p>Returns a string that is the concatenation of {@code n} copies of the string {@code s}
	 * separated by the string {@code sep}. The default value for {@code sep} is the empty string
	 * (that is, no separator). Returns the empty string if {@code n} is not positive.</p>
	 *
	 * <p>(Note that it is very easy to exhaust the memory of your machine with a single call
	 * to this function.)</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.rep} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.rep">
	 *     the Lua 5.3 Reference Manual entry for <code>string.rep</code></a>
	 */
	public static LuaFunction rep() {
		return REP;
	}

	/**
	 * Returns the function {@code string.reverse}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.reverse (s)}
	 *
	 * <p>Returns a string that is the string {@code s} reversed.</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.reverse} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.reverse">
	 *     the Lua 5.3 Reference Manual entry for <code>string.reverse</code></a>
	 */
	public static LuaFunction reverse() {
		return REVERSE;
	}

	/**
	 * Returns the function {@code string.sub}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.sub (s, i [, j])}
	 *
	 * <p>Returns the substring of {@code s} that starts at {@code i} and continues until
	 * {@code j}; {@code i} and {@code j} can be negative. If {@code j} is absent, then it
	 * is assumed to be equal to -1 (which is the same as the string length). In particular,
	 * the call {@code string.sub(s,1,j)} returns a prefix of {@code s} with length {@code j},
	 * and {@code string.sub(s, -i)} returns a suffix of {@code s} with length {@code i}.</p>
	 *
	 * <p>If, after the translation of negative indices, {@code i} is less than 1,
	 * it is corrected to 1. If {@code j} is greater than the string length, it is corrected
	 * to that length. If, after these corrections, {@code i} is greater than {@code j},
	 * the function returns the empty string.</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.sub} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.sub">
	 *     the Lua 5.3 Reference Manual entry for <code>string.sub</code></a>
	 */
	public static LuaFunction sub() {
		return SUB;
	}

	/**
	 * Returns the function {@code string.unpack}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.unpack (fmt, s [, pos])}
	 *
	 * <p>Returns the values packed in string {@code s} (see
	 * {@link #PACK {@code string.pack}}) according to the format string {@code fmt}
	 * (see §6.4.2). An optional {@code pos} marks where to start reading in {@code s}
	 * (default is 1). After the read values, this function also returns the index of the first
	 * unread byte in {@code s}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.unpack} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.unpack">
	 *     the Lua 5.3 Reference Manual entry for <code>string.unpack</code></a>
	 */
	// TODO: make public once implemented
	static LuaFunction unpack() {
		return UNPACK;
	}

	/**
	 * Returns the function {@code string.upper}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code string.upper (s)}
	 *
	 * <p>Receives a string and returns a copy of this string with all lowercase letters changed
	 * to uppercase. All other characters are left unchanged. The definition of what a lowercase
	 * letter is depends on the current locale.</p>
	 * </blockquote>
	 *
	 * @return  the {@code string.upper} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-string.upper">
	 *     the Lua 5.3 Reference Manual entry for <code>string.upper</code></a>
	 */
	public static LuaFunction upper() {
		return UPPER;
	}


	private StringLib() {
		// not to be instantiated
	}

	/**
	 * Installs the string library to the global environment {@code env} in the state
	 * context {@code context}.
	 *
	 * @param context  the state context, must not be {@code null}
	 * @param env  the global environment, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code context} or {@code env} is {@code null}
	 */
	public static void installInto(StateContext context, Table env) {
		Objects.requireNonNull(context);
		Objects.requireNonNull(env);

		Table t = context.newTable();

		t.rawset("byte", byteFn());
		t.rawset("char", charFn());
		t.rawset("dump", dump());
		t.rawset("find", find());
		t.rawset("format", format());
		t.rawset("gmatch", gmatch());
		t.rawset("gsub", gsub());
		t.rawset("len", len());
		t.rawset("lower", lower());
		t.rawset("match", match());
		t.rawset("pack", pack());
		t.rawset("packsize", packsize());
		t.rawset("rep", rep());
		t.rawset("reverse", reverse());
		t.rawset("sub", sub());
		t.rawset("unpack", unpack());
		t.rawset("upper", upper());

		// set metatable for the string type
		Table mt = context.newTable();
		mt.rawset(Metatables.MT_INDEX, t);
		context.setStringMetatable(mt);

		ModuleLib.install(env, "string", t);
	}

	private static int lowerBound(int i, int len) {
		int j = i < 0 ? len + i + 1 : i;
		return Math.max(1, j);
	}

	private static int upperBound(int i, int len) {
		int j = i < 0 ? len + i + 1 : i;
		return Math.max(0, Math.min(len, j));
	}

	private static byte toLower(byte b) {
		int c = b & 0xff;
		// FIXME: dealing with ASCII only
		return c >= 'A' && c <= 'Z' ? (byte) (c - (int) 'A' + (int) 'a') : b;
	}

	private static ByteString toLowerCase(ByteString s) {
		boolean changed = false;

		ByteStringBuilder bld = new ByteStringBuilder();
		ByteIterator it = s.byteIterator();
		while (it.hasNext()) {
			byte b = it.nextByte();
			byte c = toLower(b);
			changed |= b != c;
			bld.append(c);
		}

		return changed ? bld.toByteString() : s;
	}

	private static byte toUpper(byte b) {
		int c = b & 0xff;
		// FIXME: dealing with ASCII only
		return c >= 'a' && c <= 'z' ? (byte) (c - (int) 'a' + (int) 'A') : b;
	}

	private static ByteString toUpperCase(ByteString s) {
		boolean changed = false;

		ByteStringBuilder bld = new ByteStringBuilder();
		ByteIterator it = s.byteIterator();
		while (it.hasNext()) {
			byte b = it.nextByte();
			byte c = toUpper(b);
			changed |= b != c;
			bld.append(c);
		}

		return changed ? bld.toByteString() : s;
	}

	static class Byte extends AbstractLibFunction {

		@Override
		protected String name() {
			return "byte";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();
			int i = args.optNextInt(1);
			int j = args.optNextInt(i);

			int len = s.length();

			i = lowerBound(i, len);
			j = upperBound(j, len);

			List<Object> buf = new ArrayList<>();
			for (int idx = i; idx <= j; idx++) {
				int c = s.byteAt(idx - 1) & 0xff;
				buf.add(Long.valueOf(c));
			}
			context.getReturnBuffer().setToContentsOf(buf);
		}

	}

	static class Char extends AbstractLibFunction {

		@Override
		protected String name() {
			return "char";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			byte[] bytes = new byte[args.size()];

			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) args.nextIntRange("value", 0, 255);
			}

			ByteString s = ByteString.copyOf(bytes);
			context.getReturnBuffer().setTo(s);
		}

	}

	static class Dump extends AbstractLibFunction {

		@Override
		protected String name() {
			return "dump";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			LuaFunction f = args.nextFunction();
			boolean strip = args.optNextBoolean(false);

			throw new IllegalOperationAttemptException("unable to dump given function");
		}

	}

	static class Find extends AbstractLibFunction {

		@Override
		protected String name() {
			return "find";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			String s = args.nextString().toString();  // FIXME
			String pattern = args.nextString().toString();  // FIXME
			int init = args.optNextInt(1);
			boolean plain = args.optNextBoolean(false);

			init = lowerBound(init, s.length());

			if (plain) {
				// find a substring
				int at = s.indexOf(pattern, init - 1);
				if (at >= 0) {
					context.getReturnBuffer().setTo(
							(long) (at + 1),
							(long) (at + pattern.length()));
				}
				else {
					context.getReturnBuffer().setTo(null);
				}
			}
			else {
				// find a pattern
				StringPattern pat = StringPattern.fromString(pattern);

				StringPattern.Match m = pat.match(s, init - 1);

				if (m != null) {
					List<Object> result = new ArrayList<>();
					result.add((long) (m.beginIndex() + 1));
					result.add((long) m.endIndex());
					result.addAll(m.captures());
					context.getReturnBuffer().setToContentsOf(result);
				}
				else {
					// no match
					context.getReturnBuffer().setTo(null);
				}
			}
		}

	}

	static class Format extends AbstractLibFunction {

		@Override
		protected String name() {
			return "format";
		}

		private static class SuspendedState {

			public final String fmt;  // the format string
			public final String str;  // the string so far
			public final ArgumentIterator args;
			public final int index;

			// flags for the suspended %s
			public final int width;
			public final int flags;
			public final int precision;

			public SuspendedState(String fmt, String str, ArgumentIterator args, int index, int width, int flags, int precision) {
				this.fmt = fmt;
				this.str = str;
				this.args = args;
				this.index = index;
				this.width = width;
				this.flags = flags;
				this.precision = precision;
			}

		}

		private static String optionToString(char c) {
			if (Character.isLetterOrDigit(c)) {
				return "%" + c;
			}
			else {
				return "%<\\" + ((int) c) + ">";
			}
		}

		private static void repeatChar(char c, int num, StringBuilder bld) {
			for (int i = 0; i < num; i++) {
				bld.append(c);
			}
		}

		private static String padLeft(String s, char c, int width) {
			int diff = width - s.length();

			if (diff > 0) {
				StringBuilder bld = new StringBuilder();
				repeatChar(c, diff, bld);
				bld.append(s);
				return bld.toString();
			}
			else {
				return s;
			}
		}

		private static String padRight(String s, char c, int width) {
			int diff = width - s.length();

			if (diff > 0) {
				StringBuilder bld = new StringBuilder();
				bld.append(s);
				repeatChar(c, diff, bld);
				return bld.toString();
			}
			else {
				return s;
			}
		}

		private static final long L_1E18  = 1000000000000000000L;
		private static final long L_9E18  =  9 * L_1E18;
		private static final long L_10E18 = 10 * L_1E18;  // overflows, and that's the point

		public static String longToUnsignedString(long x) {

			// Maximum value representable by signed long is    (2^63 - 1)
			//                             by unsigned long is  (2^64 - 1)
			//
			// Now,
			//        9e18 < (2^63 - 1) < 10e18 < (2^64 - 1) < 20e18
			//
			// If signed(x) >= 0, then signed(x) == unsigned(x).
			// If signed(x) < 0, then unsigned(x) >= 2^63, and therefore unsigned(x) > unsigned(9e18).
			// Now we only need to check whether unsigned(x) >= unsigned(10e18) -- if so,
			// the leftmost digit is necessarily '1' (since 20e18 > 2^64), followed by 19 digits;
			// otherwise, the leftmost digit is '9', followed by 18 digits.
			// In 2's complement, for a, b such that both unsigned(a) >= 2^63 and unsigned(b) >= 2^63,
			// (signed(a) < signed(b)) iff (unsigned(a) < unsigned(b)),
			// so the test is equivalent to signed(x) >= signed(10e18).

			return x >= 0
					? Long.toString(x)
					: (x >= L_10E18
							? '1' + padLeft(Long.toString(x - L_10E18), '0', 19)
							: '9' + padLeft(Long.toString(x - L_9E18), '0', 18));
		}

		private int literal(String fmt, int from, StringBuilder bld) {
			int index = from;
			while (index < fmt.length()) {
				char c = fmt.charAt(index++);

				if (c != '%') {
					bld.append(c);
				}
				else {
					if (index < fmt.length() && fmt.charAt(index) == '%') {
						// literal '%'
						bld.append('%');
						index += 1;
					}
					else {
						return index;
					}
				}

			}
			return -1;
		}

		private static IllegalArgumentException invalidOptionException(char c) {
			return new IllegalArgumentException("invalid option '" + optionToString(c) + "' to 'format'");
		}

		private static int setFlag(int flags, int mask) {
			if ((flags & mask) != 0) {
				throw new IllegalArgumentException("illegal format (repeated flags)");
			}
			return flags | mask;
		}

		private static boolean hasFlag(int flags, int mask) {
			return (flags & mask) != 0;
		}

		private static String sign(boolean nonNegative, int flags) {
			return nonNegative
					? (hasFlag(flags, FLAG_SIGN_ALWAYS)
							? "+"
							: (hasFlag(flags, FLAG_ZERO_PAD)
									? " "
									: ""))
					: "-";
		}

		private static String altForm(long value, int flags, String prefix) {
			return value != 0 && hasFlag(flags, FLAG_ALT_FORM) ? prefix : "";
		}

		private static String padded(int precision, String digits) {
			return precision >= 0
					? padLeft("0".equals(digits) ? "" : digits, '0', precision)
					: digits;
		}

		private static String trimmed(int precision, String chars) {
			return precision >= 0
					? chars.substring(0, Math.min(chars.length(), precision))
					: chars;
		}

		private static String justified(int width, int flags, String digits) {
			return width >= 0
					? (hasFlag(flags, FLAG_LEFTJUSTIFY)
							? padRight(digits, ' ', width)
							: padLeft(digits, ' ', width))
					: digits;
		}

		private static final int FLAG_LEFTJUSTIFY = 1 << 1;
		private static final int FLAG_SIGN_ALWAYS = 1 << 2;
		private static final int FLAG_SIGN_SPACE = 1 << 3;
		private static final int FLAG_ZERO_PAD = 1 << 4;
		private static final int FLAG_ALT_FORM = 1 << 5;

		private static void format_signed_integer(StringBuilder bld, ArgumentIterator args, char spec, int width, int flags, int precision) {
			long l = args.nextInteger();

			String ls = LuaFormat.toString(l);
			String digits = l < 0 ? ls.substring(1) : ls;  // ignore the sign, we'll re-attach it later
			bld.append(justified(width, flags,
					sign(l >= 0, flags) + padded(precision, digits)));
		}

		private static void format_unsigned_integer(StringBuilder bld, ArgumentIterator args, int width, int flags, int precision) {
			long l = args.nextInteger();

			String digits = longToUnsignedString(l);
			bld.append(justified(width, flags, padded(precision, digits)));
		}

		private static void format_octal_integer(StringBuilder bld, ArgumentIterator args, int width, int flags, int precision) {
			long l = args.nextInteger();

			String digits = Long.toOctalString(l);
			bld.append(justified(width, flags, altForm(l, flags, "0") + padded(precision, digits)));
		}

		private static void format_hex_integer(StringBuilder bld, ArgumentIterator args, boolean uppercase, int width, int flags, int precision) {
			long l = args.nextInteger();

			String digits = Long.toHexString(l);
			String lowerCaseResult = justified(width, flags,
					altForm(l, flags, "0x") + padded(precision, digits));

			bld.append(uppercase ? lowerCaseResult.toUpperCase() : lowerCaseResult);
		}

		private static void format_char(StringBuilder bld, ArgumentIterator args, int width, int flags) {
			bld.append(justified(width, flags, Character.toString((char) args.nextInteger())));
		}

		private static void format_float(StringBuilder bld, ArgumentIterator args, char spec, int width, int flags, int precision) {
			double v = args.nextFloat();

			if (Double.isNaN(v) || Double.isInfinite(v)) {
				final ByteString chars;

				chars = Double.isNaN(v)
						? LuaFormat.NAN
						: ByteString.of(sign(v > 0, flags) + LuaFormat.INF);

				bld.append(justified(width, flags, chars.toString()));
			}
			else {
				StringBuilder fmtBld = new StringBuilder();
				fmtBld.append('%');
				if (hasFlag(flags, FLAG_LEFTJUSTIFY)) fmtBld.append('-');
				if (hasFlag(flags, FLAG_SIGN_ALWAYS)) fmtBld.append('+');
				if (hasFlag(flags, FLAG_SIGN_SPACE)) fmtBld.append(' ');
				if (hasFlag(flags, FLAG_ZERO_PAD)) fmtBld.append('0');
				if (hasFlag(flags, FLAG_ALT_FORM)) fmtBld.append('#');

				if (width > 0) fmtBld.append(width);
				// width required by Formatter, but not supplied
				else if (hasFlag(flags, FLAG_ZERO_PAD)) fmtBld.append('1');

				if (precision > 0) fmtBld.append('.').append(precision);
				fmtBld.append(spec);
				String formatted = String.format(fmtBld.toString(), v);

				if (spec == 'a' || spec == 'A') {
					// insert the '+' sign to the exponent
					int p = formatted.indexOf(spec == 'a' ? 'p' : 'P') + 1;
					if (formatted.charAt(p) != '-') {
						formatted = formatted.substring(0, p) + '+' + formatted.substring(p);
					}
				}

				bld.append(formatted);
			}
		}

		private void format_s(ExecutionContext context, String fmt, StringBuilder bld, ArgumentIterator args, int index, int width, int flags, int precision)
				throws ResolvedControlThrowable {
			Object v = args.nextAny();
			final String s;

			ByteString stringValue = Conversions.stringValueOf(v);
			if (stringValue != null) {
				s = stringValue.toString();
			}
			else {
				Object metamethod = Metatables.getMetamethod(context, BasicLib.MT_TOSTRING, v);
				if (metamethod != null) {
					// call __tostring
					try {
						Dispatch.call(context, metamethod, v);
					}
					catch (UnresolvedControlThrowable ct) {
						throw ct.resolve(this, new SuspendedState(fmt, bld.toString(), args, index, width, flags, precision));
					}
					resume_s(context, bld, width, flags, precision);
					return;
				}
				else {
					s = Conversions.toHumanReadableString(v).toString();
				}
			}
			bld.append(justified(width, flags, trimmed(precision, s)));
		}

		private static void resume_s(ExecutionContext context, StringBuilder bld, int width, int flags, int precision) {
			Object o = context.getReturnBuffer().get0();
			ByteString sv = Conversions.stringValueOf(o);
			String s = sv != null ? sv.toString() : "";
			bld.append(justified(width, flags, trimmed(precision, s)));
		}

		private void format_q(StringBuilder bld, ArgumentIterator args) {
			Object o = args.nextAny();
			final String s;

			if (o == null) s = LuaFormat.NIL.toString();
			else if (o instanceof Boolean) s = LuaFormat.toString(((Boolean) o).booleanValue());
			else if (o instanceof String) s = LuaFormat.escape((String) o);
			else if (o instanceof ByteString) s = LuaFormat.escape(((ByteString) o).toString());
			else if (o instanceof Number) s = Conversions.stringValueOf((Number) o).toString();
			else {
				throw new BadArgumentException(args.at(), name(), "value has no literal form");
			}

			bld.append(s);
		}

		private int placeholder(ExecutionContext context, String fmt, int from, StringBuilder bld, ArgumentIterator args)
				throws ResolvedControlThrowable {

			if (!args.hasNext()) {
				throw new BadArgumentException(args.size() + 1, name(), "no value");
			}

			int index = from;

			char c;

			int flags = 0;

			// flags
			{
				boolean wasFlag = true;

				do {
					if (index < fmt.length()) {
						c = fmt.charAt(index++);
					}
					else {
						throw invalidOptionException('\0');
					}

					switch (c) {
						case '-': flags = setFlag(flags, FLAG_LEFTJUSTIFY); break;
						case '+': flags = setFlag(flags, FLAG_SIGN_ALWAYS); break;
						case ' ': flags = setFlag(flags, FLAG_SIGN_SPACE); break;
						case '0': flags = setFlag(flags, FLAG_ZERO_PAD); break;
						case '#': flags = setFlag(flags, FLAG_ALT_FORM); break;

						default:
							// not a flag, take the character back
							index -= 1;
							wasFlag = false;
							break;
					}

				} while (wasFlag);
			}

			// width
			int width = -1;

			{
				boolean wasWidth = true;

				do {
					if (index < fmt.length()) {
						c = fmt.charAt(index++);
					}
					else {
						throw invalidOptionException('\0');
					}

					if (c >= '0' && c <= '9') {
						width = Math.max(0, width) * 10 + (c - '0');
						if (width >= 100) {
							throw new IllegalArgumentException("illegal format (width or precision too long)");
						}
					}
					else {
						// not a width specifier, put back
						index -= 1;
						wasWidth = false;
					}

				} while (wasWidth);
			}

			// precision
			int precision = -1;

			{
				if (index < fmt.length() && fmt.charAt(index) == '.') {
					index += 1;  // skip the '.'
					precision = 0;

					boolean wasPrecision = true;
					do {
						if (index < fmt.length()) {
							c = fmt.charAt(index++);
						}
						else {
							throw invalidOptionException('\0');
						}

						if (c >= '0' && c <= '9') {
							precision = precision * 10 + (c - '0');
							if (precision >= 100) {
								throw new IllegalArgumentException("illegal format (width or precision too long)");
							}
						}
						else {
							// not a width specifier, put back
							index -= 1;
							wasPrecision = false;
						}

					} while (wasPrecision);

				}
			}

			// type
			{
				char d = fmt.charAt(index++);

				switch (d) {

					case 'd':
					case 'i':
						format_signed_integer(bld, args, d, width, flags, precision);
						break;

					case 'u':
						format_unsigned_integer(bld, args, width, flags, precision);
						break;

					case 'o':
						format_octal_integer(bld, args, width, flags, precision);
						break;

					case 'x':
					case 'X':
						format_hex_integer(bld, args, d == 'X', width, flags, precision);
						break;

					case 'c':
						format_char(bld, args, width, flags);
						break;

					case 'f':
					case 'a':
					case 'A':
					case 'e':
					case 'E':
					case 'g':
					case 'G':
						format_float(bld, args, d, width, flags, precision);
						break;

					case 's':
						format_s(context, fmt, bld, args, index, width, flags, precision);
						break;

					case 'q':
						format_q(bld, args);
						break;

					default:
						throw new IllegalArgumentException("invalid option '" + optionToString(d) + "' to 'format'");

				}

			}

			return index < fmt.length() ? index : -1;
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			String fmt = args.nextString().toString();  // FIXME
			StringBuilder bld = new StringBuilder();
			run(context, fmt, args, bld, 0);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			SuspendedState ss = (SuspendedState) suspendedState;

			StringBuilder bld = new StringBuilder(ss.str);

			// resume the suspended %s
			resume_s(context, bld, ss.width, ss.flags, ss.precision);

			// continue the loop
			run(context, ss.fmt, ss.args, bld, ss.index);
		}

		private void run(ExecutionContext context, String fmt, ArgumentIterator args, StringBuilder bld, int idx)
				throws ResolvedControlThrowable {
			do {
				idx = literal(fmt, idx, bld);
				if (idx >= 0) {
					idx = placeholder(context, fmt, idx, bld, args);
				}
			} while (idx >= 0);

			context.getReturnBuffer().setTo(bld.toString());
		}

	}

	static class GMatch extends AbstractLibFunction {

		static class IteratorFunction extends AbstractFunction0 {

			public final String string;
			public final StringPattern pattern;
			private final AtomicInteger index;

			public IteratorFunction(String string, StringPattern pattern) {
				this.string = Check.notNull(string);
				this.pattern = Check.notNull(pattern);
				this.index = new AtomicInteger(0);
			}

			@Override
			public void invoke(ExecutionContext context) throws ResolvedControlThrowable {

				int idx = index.get();

				if (idx >= 0) {
					StringPattern.Match m = pattern.match(string, idx);

					if (m != null) {
						// found a match
						int endIndex = m.endIndex();
						if (endIndex == idx) {
							// avoid looping on empty matches
							endIndex += 1;
						}

						index.set(endIndex);

						if (!m.captures().isEmpty()) {
							context.getReturnBuffer().setToContentsOf(m.captures());
						}
						else {
							context.getReturnBuffer().setTo(m.fullMatch());
						}
					}
					else {
						// no match; go to end state
						index.set(-1);
						context.getReturnBuffer().setTo();
					}
				}
				else {
					// in end state
					context.getReturnBuffer().setTo();
				}
			}

			@Override
			public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
				throw new NonsuspendableFunctionException(this.getClass());
			}
		}

		@Override
		protected String name() {
			return "gmatch";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			String s = args.nextString().toString();  // FIXME
			String pattern = args.nextString().toString();  // FIXME

			StringPattern pat = StringPattern.fromString(pattern, true);

			LuaFunction f = new IteratorFunction(s, pat);

			context.getReturnBuffer().setTo(f);
		}

	}

	static class GSub extends AbstractLibFunction {

		private static final String ARG3_ERROR_MESSAGE = "string/function/table expected";

		@Override
		protected String name() {
			return "gsub";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			String s = args.nextString().toString();  // FIXME
			String pattern = args.nextString().toString();  // FIXME

			final Object repl;
			if (!args.hasNext()) {
				throw args.badArgument(3, ARG3_ERROR_MESSAGE);
			}
			else {
				Object o = args.nextAny();

				// a string?
				ByteString replStr = Conversions.stringValueOf(o);
				if (replStr != null) {
					repl = replStr.toString();
				}
				else if (o instanceof Table || o instanceof LuaFunction) {
					repl = o;
				}
				else {
					throw args.badArgument(3, ARG3_ERROR_MESSAGE);
				}
			}

			int n = args.optNextInt(Integer.MAX_VALUE);

			StringPattern pat = StringPattern.fromString(pattern);

			run(context, s, 0, new StringBuilder(), pat, 0, n, repl);
		}

		private static class State {

			public final String str;
			public final StringPattern pat;
			public final int count;
			public final int num;

			public final Object repl;

			public final StringBuilder bld;
			public final String fullMatch;
			public final int idx;

			private State(String str, StringPattern pat, int count, int num, Object repl, StringBuilder bld, String fullMatch, int idx) {
				this.str = str;
				this.pat = pat;
				this.count = count;
				this.num = num;
				this.repl = repl;
				this.bld = bld;
				this.fullMatch = fullMatch;
				this.idx = idx;
			}

		}

		private void run(ExecutionContext context, String str, int idx, StringBuilder bld, StringPattern pat, int count, int num, Object repl)
				throws ResolvedControlThrowable {

			while (count < num) {
				StringPattern.Match m = pat.match(str, idx);

				if (m == null) {
					// no more matches
					break;
				}

				count += 1;

				// non-matching prefix
				if (idx < m.beginIndex()) {
					bld.append(str.substring(idx, m.beginIndex()));
				}

				List<Object> captures = m.captures().isEmpty()
						? Collections.singletonList((Object) m.fullMatch())
						: m.captures();

				// avoid looping indefinitely for empty matches
				idx = m.endIndex() != idx ? m.endIndex() : m.endIndex() + 1;

				if (repl instanceof String) {
					String r = stringReplace((String) repl, m.fullMatch(), captures);
					bld.append(r);
				}
				else {
					// NOTE: throws and handles ControlThrowables
					nonStringReplace(
							context, str, pat, idx, count, num, bld,
							repl, m.fullMatch(), captures);
				}
			}

			// non-matching suffix
			if (idx < str.length()) {
				bld.append(str.substring(idx, str.length()));
			}

			context.getReturnBuffer().setTo(bld.toString(), (long) count);
		}

		private static String stringReplace(String s, String fullMatch, List<Object> captures) {
			StringBuilder bld = new StringBuilder();

			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);

				if (c == '%' && i + 1 < s.length()) {
					char d = s.charAt(i + 1);
					i += 1;  // skip the escape

					if (d >= '0' && d <= '9') {
						int idx = (int) d - (int) '0';
						if (idx == 0) {
							bld.append(fullMatch);
						}
						else {
							if (idx - 1 < captures.size()) {
								// captures are either strings or integers
								ByteString sv = Conversions.stringValueOf(captures.get(idx - 1));
								assert (sv != null);
								bld.append(sv);
							}
							else {
								// no capture with this index
								bld.append(d);
							}
						}
					}
					else {
						bld.append(d);
					}
				}
				else {
					bld.append(c);
				}
			}

			return bld.toString();
		}

		private void nonStringReplace(
				ExecutionContext context,
				String str,
				StringPattern pat,
				int idx,
				int count,
				int num,
				StringBuilder bld,
				Object repl,
				String fullMatch,
				List<Object> captures)
				throws ResolvedControlThrowable {

			assert (!captures.isEmpty());

			Object cap = captures.get(0);

			try {
				if (repl instanceof Table) {
					Dispatch.index(context, (Table) repl, cap);
				}
				else if (repl instanceof LuaFunction) {
					Dispatch.call(context, (LuaFunction) repl, (Object[]) captures.toArray());
				}
				else {
					throw new IllegalStateException("Illegal replacement: " + repl);
				}
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, new State(str, pat, count, num, repl, bld, fullMatch, idx));
			}
			resumeReplace(context, bld, fullMatch);
		}

		private static void resumeReplace(ExecutionContext context, StringBuilder bld, String fullMatch) {
			Object value = context.getReturnBuffer().get0();
			ByteString sv = Conversions.stringValueOf(value);
			if (sv != null) {
				bld.append(sv);
			}
			else {
				if (!Conversions.booleanValueOf(value)) {
					// false or nil
	                bld.append(fullMatch);
				}
				else {
					throw new LuaRuntimeException("invalid replacement value (a "
							+ PlainValueTypeNamer.INSTANCE.typeNameOf(value) + ")");
				}
			}
		}


		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			State state = (State) suspendedState;
			resumeReplace(context, state.bld, state.fullMatch);
			run(context, state.str, state.idx, state.bld, state.pat, state.count, state.num, state.repl);
		}

	}

	static class Len extends AbstractLibFunction {

		@Override
		protected String name() {
			return "len";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();
			context.getReturnBuffer().setTo((long) s.length());
		}

	}

	static class Lower extends AbstractLibFunction {

		@Override
		protected String name() {
			return "lower";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();
			context.getReturnBuffer().setTo(toLowerCase(s));
		}

	}

	static class Match extends AbstractLibFunction {

		@Override
		protected String name() {
			return "match";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			String s = args.nextString().toString();  // FIXME
			String pattern = args.nextString().toString();  // FIXME
			int init = args.optNextInt(1);

			init = lowerBound(init, s.length());

			StringPattern pat = StringPattern.fromString(pattern);

			StringPattern.Match m = pat.match(s, init - 1);
			if (m != null) {
				if (m.captures().isEmpty()) {
					context.getReturnBuffer().setTo(m.fullMatch());
				}
				else {
					context.getReturnBuffer().setToContentsOf(m.captures());
				}
			}
			else {
				// no match
				context.getReturnBuffer().setTo(null);
			}
		}

	}

	static class Pack extends UnimplementedFunction {

		// TODO

		public Pack() {
			super("string.pack");
		}

	}

	static class PackSize extends UnimplementedFunction {

		// TODO

		public PackSize() {
			super("string.packsize");
		}

	}

	static class Rep extends AbstractLibFunction {

		@Override
		protected String name() {
			return "rep";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();
			int n = args.nextInt();
			ByteString sep = args.hasNext() ? args.nextString() : ByteString.empty();

			final ByteString result;
			if (n > 0) {
				ByteStringBuilder bld = new ByteStringBuilder();

				for (int i = 0; i < n; i++) {
					bld.append(s);
					if (i + 1 < n) {
						bld.append(sep);
					}
				}

				result = bld.toByteString();
			}
			else {
				result = ByteString.empty();
			}

			context.getReturnBuffer().setTo(result);
		}

	}

	static class Reverse extends AbstractLibFunction {

		@Override
		protected String name() {
			return "reverse";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();

			byte[] bytes = s.getBytes();
			for (int i = 0; i < bytes.length / 2; i++) {
				int j = bytes.length - 1 - i;

				byte tmp = bytes[i];
				bytes[i] = bytes[j];
				bytes[j] = tmp;
			}

			ByteString result = ByteString.copyOf(bytes);

			context.getReturnBuffer().setTo(result);
		}

	}

	static class Sub extends AbstractLibFunction {

		@Override
		protected String name() {
			return "sub";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();
			int i = args.nextInt();
			int j = args.optNextInt(-1);

			int len = s.length();
			i = lowerBound(i, len) - 1;
			j = upperBound(j, len);

			ByteString result = i < j ? s.substring(i, j) : ByteString.empty();

			context.getReturnBuffer().setTo(result);
		}

	}

	static class Unpack extends UnimplementedFunction {

		// TODO

		public Unpack() {
			super("string.unpack");
		}

	}

	static class Upper extends AbstractLibFunction {

		@Override
		protected String name() {
			return "upper";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString s = args.nextString();
			context.getReturnBuffer().setTo(toUpperCase(s));
		}

	}

}
