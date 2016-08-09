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
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.Table;

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
public abstract class StringLib implements Lib {

	@Override
	public void installInto(LuaState state, Table env) {
		LibUtils.setIfNonNull(env, "byte", _byte());
		LibUtils.setIfNonNull(env, "char", _char());
		LibUtils.setIfNonNull(env, "dump", _dump());
		LibUtils.setIfNonNull(env, "find", _find());
		LibUtils.setIfNonNull(env, "format", _format());
		LibUtils.setIfNonNull(env, "gmatch", _gmatch());
		LibUtils.setIfNonNull(env, "gsub", _gsub());
		LibUtils.setIfNonNull(env, "len", _len());
		LibUtils.setIfNonNull(env, "lower", _lower());
		LibUtils.setIfNonNull(env, "match", _match());
		LibUtils.setIfNonNull(env, "pack", _pack());
		LibUtils.setIfNonNull(env, "packsize", _packsize());
		LibUtils.setIfNonNull(env, "rep", _rep());
		LibUtils.setIfNonNull(env, "reverse", _reverse());
		LibUtils.setIfNonNull(env, "sub", _sub());
		LibUtils.setIfNonNull(env, "unpack", _unpack());
		LibUtils.setIfNonNull(env, "upper", _upper());

		// set metatable for the string type
		Table mt = state.newTable();
		mt.rawset(Metatables.MT_INDEX, env);
		state.setStringMetatable(mt);
	}

	/**
	 * {@code string.byte (s [, i [, j]])}
	 *
	 * <p>Returns the internal numeric codes of the characters
	 * {@code s[i]}, {@code s[i+1]}, ..., {@code s[j]}. The default value for {@code i} is 1;
	 * the default value for {@code j} is {@code i}. These indices are corrected following
	 * the same rules of function {@link #_sub <code>string.sub</code>}.</p>
	 *
	 * <p>Numeric codes are not necessarily portable across platforms.</p>
	 */
	public abstract Function _byte();

	/**
	 * {@code string.char (···)}
	 *
	 * <p>Receives zero or more integers. Returns a string with length equal to the number
	 * of arguments, in which each character has the internal numeric code equal
	 * to its corresponding argument.</p>
	 *
	 * <p>Numeric codes are not necessarily portable across platforms.</p>
	 */
	public abstract Function _char();

	/**
	 * {@code string.dump (function [, strip])}
	 *
	 * <p>Returns a string containing a binary representation (a binary chunk) of the given
	 * function, so that a later {@link BasicLib#_load() <code>load</code>} on this string
	 * returns a copy of the function (but with new upvalues). If {@code strip} is a true value,
	 * the binary representation may not include all debug information about the function,
	 * to save space.</p>
	 *
	 * <p>Functions with upvalues have only their number of upvalues saved. When (re)loaded,
	 * those upvalues receive fresh instances containing <b>nil</b>. (You can use the debug
	 * library to serialize and reload the upvalues of a function in a way adequate
	 * to your needs.)</p>
	 */
	public abstract Function _dump();

	/**
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
	 */
	public abstract Function _find();

	/**
	 * {@code string.format (formatstring, ···)}
	 *
	 * <p>Returns a formatted version of its variable number of arguments following
	 * the description given in its first argument (which must be a string). The format string
	 * follows the same rules as the ISO C function {@code sprintf}. The only differences
	 * are that the options/modifiers {@code *}, {@code h}, {@code L}, {@code l}, {@code n},
	 * and {@code p} are not supported and that there is an extra option, {@code q}.
	 * The {@code q} option formats a string between double quotes, using escape sequences
	 * when necessary to ensure that it can safely be read back by the Lua interpreter.
	 * For instance, the call
	 *
	 * <pre>
	 * string.format('%q', 'a string with "quotes" and \n new line')
	 * </pre>
	 *
	 * may produce the string:
	 *
	 * <pre>
	 * "a string with \"quotes\" and \
	 * new line"}
	 * </pre>
	 * </p>
	 *
	 * Options {@code A}, {@code a}, {@code E}, {@code e}, {@code f}, {@code G}, and {@code g}
	 * all expect a number as argument. Options {@code c}, {@code d}, {@code i}, {@code o},
	 * {@code u}, {@code X}, and {@code x} expect an integer. Option {@code q} expects a string.
	 * Option {@code s} expects a string; if its argument is not a string, it is converted
	 * to one following the same rules of {@link BasicLib#_tostring() <code>tostring</code>}.
	 * If the option has any modifier (flags, width, length), the string argument should
	 * not contain embedded zeros. When Lua is compiled with a non-C99 compiler, options
	 * {@code A} and {@code a} (hexadecimal floats) do not support any modifier (flags, width,
	 * length).
	 */
	public abstract Function _format();

	/**
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
	 * s = "hello world from Lua"
	 * for w in string.gmatch(s, "%a+") do
	 *   print(w)
	 * end
	 * </pre>
	 * </p>
	 *
	 * <p>The next example collects all pairs {@code key=value} from the given string into
	 * a table:</p>
	 *
	 * <pre>
	 * t = {}
	 * s = "from=world, to=Lua"
	 * for k, v in string.gmatch(s, "(%w+)=(%w+)") do
	 *   t[k] = v
	 * end
	 * </pre>
	 * </p>
	 *
	 * <p>For this function, a caret '{@code ^}' at the start of a pattern does not work as
	 * an anchor, as this would prevent the iteration.</p>
	 */
	public abstract Function _gmatch();

	/**
	 * {@code string.gsub (s, pattern, repl [, n])}
	 *
	 * <p>Returns a copy of {@code s} in which all (or the first {@code n}, if given) occurrences
	 * of the pattern (see §6.4.1) have been replaced by a replacement string specified
	 * by {@code repl}, which can be a string, a table, or a function. {@code gsub} also returns,
	 * as its second value, the total number of matches that occurred. The name {@code gsub} comes
	 * from <i>Global SUBstitution<i></i>.</p>
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
	 * <p>Here are some examples:
	 *
	 * <pre>
	 * x = string.gsub("hello world", "(%w+)", "%1 %1")
	 * --> x="hello hello world world"
	 * </pre>
	 *
	 * <pre>
	 * x = string.gsub("hello world", "%w+", "%0 %0", 1)
	 * --> x="hello hello world"
	 * </pre>
	 *
	 * <pre>
	 * x = string.gsub("hello world from Lua", "(%w+)%s*(%w+)", "%2 %1")
	 * --> x="world hello Lua from"
	 * </pre>
	 *
	 * <pre>
	 * x = string.gsub("home = $HOME, user = $USER", "%$(%w+)", os.getenv)
	 * --> x="home = /home/roberto, user = roberto"
	 * </pre>
	 *
	 * <pre>
	 * x = string.gsub("4+5 = $return 4+5$", "%$(.-)%$", function (s)
	 *       return load(s)()
	 *     end)
	 * --> x="4+5 = 9"
	 * </pre>
	 *
	 * <pre>
	 * local t = {name="lua", version="5.3"}
	 * x = string.gsub("$name-$version.tar.gz", "%$(%w+)", t)
	 * --> x="lua-5.3.tar.gz"
	 * </pre>
	 * </p>
	 */
	public abstract Function _gsub();

	/**
	 * {@code string.len (s)}
	 *
	 * <p>Receives a string and returns its length. The empty string {@code ""} has length 0.
	 * Embedded zeros are counted, so {@code "a\000bc\000"} has length 5.</p>
	 */
	public abstract Function _len();

	/**
	 * {@code string.lower (s)}
	 *
	 * <p>Receives a string and returns a copy of this string with all uppercase letters changed
	 * to lowercase. All other characters are left unchanged. The definition of what an uppercase
	 * letter is depends on the current locale.</p>
	 */
	public abstract Function _lower();

	/**
	 * {@code string.match (s, pattern [, init])}
	 *
	 * <p>Looks for the first match of {@code pattern} (see §6.4.1) in the string {@code s}.
	 * If it finds one, then {@code match} returns the captures from the pattern; otherwise
	 * it returns <b>nil</b>. If {@code pattern} specifies no captures, then the whole match
	 * is returned. A third, optional numeric argument {@code init} specifies where to start
	 * the search; its default value is 1 and can be negative.</p>
	 */
	public abstract Function _match();

	/**
	 * {@code string.pack (fmt, v1, v2, ···)}
	 *
	 * <p>Returns a binary string containing the values {@code v1}, {@code v2}, etc. packed
	 * (that is, serialized in binary form) according to the format string {@code fmt}
	 * (see §6.4.2).</p>
	 */
	public abstract Function _pack();

	/**
	 * {@code string.packsize (fmt)}
	 *
	 * <p>Returns the size of a string resulting from {@link #_pack() <code>string.pack</code>}
	 * with the given format. The format string cannot have the variable-length options
	 * '{@code s}' or '{@code z}' (see §6.4.2).</p>
	 */
	public abstract Function _packsize();

	/**
	 * {@code string.rep (s, n [, sep])}
	 *
	 * <p>Returns a string that is the concatenation of {@code n} copies of the string {@code s}
	 * separated by the string {@code sep}. The default value for {@code sep} is the empty string
	 * (that is, no separator). Returns the empty string if {@code n} is not positive.</p>
	 *
	 * <p>(Note that it is very easy to exhaust the memory of your machine with a single call
	 * to this function.)</p>
	 */
	public abstract Function _rep();

	/**
	 * {@code string.reverse (s)}
	 *
	 * <p>Returns a string that is the string {@code s} reversed.</p>
	 */
	public abstract Function _reverse();

	/**
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
	 */
	public abstract Function _sub();

	/**
	 * {@code string.unpack (fmt, s [, pos])}
	 *
	 * <p>Returns the values packed in string {@code s} (see
	 * {@link #_pack() <code>string.pack</code>}) according to the format string {@code fmt}
	 * (see §6.4.2). An optional {@code pos} marks where to start reading in {@code s}
	 * (default is 1). After the read values, this function also returns the index of the first
	 * unread byte in {@code s}.</p>
	 */
	public abstract Function _unpack();

	/**
	 * {@code string.upper (s)}
	 *
	 * <p>Receives a string and returns a copy of this string with all lowercase letters changed
	 * to uppercase. All other characters are left unchanged. The definition of what a lowercase
	 * letter is depends on the current locale.</p>
	 */
	public abstract Function _upper();

}
