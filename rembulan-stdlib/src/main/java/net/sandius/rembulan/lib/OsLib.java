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
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.impl.NonsuspendableFunctionException;
import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.runtime.AbstractFunction0;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.util.Objects;

/**
 * This library is implemented through table {@code os}.
 */
public final class OsLib {

	private OsLib() {
		// not to be instantiated
	}

	public static void installInto(StateContext context, Table env, RuntimeEnvironment runtimeEnvironment) {
		Table t = context.newTable();

		t.rawset("clock", new Clock(runtimeEnvironment));
		t.rawset("date", new Date(runtimeEnvironment));
		t.rawset("difftime", new DiffTime(runtimeEnvironment));
		t.rawset("execute", new Execute(runtimeEnvironment));
		t.rawset("exit", new Exit(runtimeEnvironment));
		t.rawset("getenv", new GetEnv(runtimeEnvironment));
		t.rawset("remove", new Remove(runtimeEnvironment));
		t.rawset("rename", new Rename(runtimeEnvironment));
		t.rawset("setlocale", new SetLocale(runtimeEnvironment));
		t.rawset("time", new Time(runtimeEnvironment));
		t.rawset("tmpname", new TmpName(runtimeEnvironment));

		ModuleLibHelper.install(env, "os", t);
	}

	/**
	 * {@code os.clock ()}
	 *
	 * <p>Returns an approximation of the amount in seconds of CPU time used by the program.</p>
	 */
	public static class Clock extends AbstractFunction0 {

		private final RuntimeEnvironment environment;

		public Clock(RuntimeEnvironment environment) {
			this.environment = Objects.requireNonNull(environment);
		}

		@Override
		public void invoke(ExecutionContext context) throws ResolvedControlThrowable {
			context.getReturnBuffer().setTo(environment.getCpuTime());
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	/**
	 * {@code os.date ([format [, time]])}
	 *
	 * <p>Returns a string or a table containing date and time, formatted according to the given
	 * string format.</p>
	 *
	 * <p>If the time argument is present, this is the time to be formatted (see the
	 * {@link Time {@code os.time}} function for a description of this value).
	 * Otherwise, {@code date} formats the current time.</p>
	 *
	 * <p>If format starts with '{@code !}', then the date is formatted in Coordinated Universal
	 * Time. After this optional character, if format is the string {@code "*t"}, then {@code date}
	 * returns a table with the following fields: {@code year}, {@code month} (1–12),
	 * {@code day} (1–31), {@code hour} (0–23), {@code min} (0–59), {@code sec} (0–61),
	 * {@code wday} (weekday, Sunday is 1), {@code yday} (day of the year), and {@code isdst}
	 * (daylight saving flag, a boolean). This last field may be absent if the information
	 * is not available.</p>
	 *
	 * <p>If format is not {@code "*t"}, then date returns the date as a string, formatted
	 * according to the same rules as the ISO C function {@code strftime}.</p>
	 *
	 * <p>When called without arguments, date returns a reasonable date and time representation
	 * that depends on the host system and on the current locale. (More specifically,
	 * {@code os.date()} is equivalent to {@code os.date("%c")}.)</p>
	 *
	 * <p>On non-POSIX systems, this function may be not thread safe because of its reliance
	 * on C function {@code gmtime} and C function {@code localtime}.</p>
	 */
	public static class Date extends UnimplementedFunction {
		// TODO
		Date(RuntimeEnvironment runtimeEnvironment) {
			super("os.date");
		}
	}

	/**
	 * {@code os.difftime (t2, t1)}
	 *
	 * <p>Returns the difference, in seconds, from time {@code t1} to time {@code t2} (where
	 * the times are values returned by {@code os.time}). In POSIX, Windows, and some other
	 * systems, this value is exactly {@code t2}-{@code t1}.</p>
	 */
	static class DiffTime extends UnimplementedFunction {
		// TODO
		public DiffTime(RuntimeEnvironment runtimeEnvironment) {
			super("os.difftime");
		}
	}

	/**
	 * {@code os.execute ([command])}
	 *
	 * <p>This function is equivalent to the ISO C function {@code system}.
	 * It passes {@code command} to be executed by an operating system shell. Its first result
	 * is <b>true</b> if the command terminated successfully, or <b>nil</b> otherwise.
	 * After this first result the function returns a string plus a number, as follows:</p>
	 * <ul>
	 * <li><b>{@code "exit"}</b>: the command terminated normally; the following number
	 * is the exit status of the command.</li>
	 * <li><b>{@code "signal"}</b>: the command was terminated by a signal; the following number
	 * is the signal that terminated the command.</li>
	 * </ul>
	 * <p>When called without a {@code command}, {@code os.execute} returns a boolean that
	 * is <b>true</b> if a shell is available.</p>
	 */
	static class Execute extends UnimplementedFunction {
		// TODO
		public Execute(RuntimeEnvironment runtimeEnvironment) {
			super("os.execute");
		}
	}

	/**
	 * {@code os.exit ([code [, close]])}
	 *
	 * <p>Calls the ISO C function {@code exit} to terminate the host program. If {@code code}
	 * is <b>true</b>, the returned status is {@code EXIT_SUCCESS}; if code is <b>false</b>,
	 * the returned status is {@code EXIT_FAILURE}; if code is a number, the returned status
	 * is this number. The default value for code is <b>true</b>.</p>
	 *
	 * <p>If the optional second argument {@code close} is <b>true</b>, closes the Lua state
	 * before exiting.</p>
	 */
	static class Exit extends UnimplementedFunction {
		// TODO
		public Exit(RuntimeEnvironment runtimeEnvironment) {
			super("os.exit");
		}
	}

	/**
	 * {@code os.getenv (varname)}
	 *
	 * <p>Returns the value of the process environment variable {@code varname}, or <b>nil</b>
	 * if the variable is not defined.</p>
	 */
	public static class GetEnv extends AbstractLibFunction {

		private final RuntimeEnvironment environment;

		public GetEnv(RuntimeEnvironment environment) {
			this.environment = Objects.requireNonNull(environment);
		}

		@Override
		protected String name() {
			return "getenv";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString name = args.nextString();
			String value = environment.getEnv(name.toString());
			context.getReturnBuffer().setTo(value);
		}

	}

	/**
	 * {@code os.remove (filename)}
	 *
	 * <p>Deletes the file (or empty directory, on POSIX systems) with the given name.
	 * If this function fails, it returns <b>nil</b>, plus a string describing the error
	 * and the error code.</p>
	 */
	static class Remove extends UnimplementedFunction {
		// TODO
		public Remove(RuntimeEnvironment runtimeEnvironment) {
			super("os.remove");
		}
	}

	/**
	 * {@code os.rename (oldname, newname)}
	 *
	 * <p>Renames file or directory named {@code oldname} to {@code newname}. If this function
	 * fails, it returns <b>nil</b>, plus a string describing the error and the error code.</p>
	 */
	static class Rename extends UnimplementedFunction {
		// TODO
		public Rename(RuntimeEnvironment runtimeEnvironment) {
			super("os.rename");
		}
	}

	/**
	 * {@code os.setlocale (locale [, category])}
	 *
	 * <p>Sets the current locale of the program. {@code locale} is a system-dependent string
	 * specifying a locale; {@code category} is an optional string describing which category
	 * to change: {@code "all"}, {@code "collate"}, {@code "ctype"}, {@code "monetary"},
	 * {@code "numeric"}, or {@code "time"}; the default category is {@code "all"}.
	 * The function returns the name of the new locale, or <b>nil</b> if the request cannot
	 * be honored.</p>
	 *
	 * <p>If {@code locale} is the empty string, the current locale is set to
	 * an implementation-defined native locale. If locale is the string {@code "C"},
	 * the current locale is set to the standard C locale.</p>
	 *
	 * <p>When called with <b>nil</b> as the first argument, this function only returns the name
	 * of the current locale for the given category.</p>
	 *
	 * <p>This function may be not thread safe because of its reliance on C function
	 * {@code setlocale}.</p>
	 */
	static class SetLocale extends UnimplementedFunction {
		// TODO
		public SetLocale(RuntimeEnvironment runtimeEnvironment) {
			super("os.setlocale");
		}
	}

	/**
	 * {@code os.time ([table])}
	 *
	 * <p>Returns the current time when called without arguments, or a time representing
	 * the local date and time specified by the given {@code table}. This table must have fields
	 * {@code year}, {@code month}, and {@code day}, and may have fields {@code hour}
	 * (default is 12), {@code min} (default is 0), {@code sec} (default is 0), and {@code isdst}
	 * (default is <b>nil</b>). Other fields are ignored. For a description of these fields,
	 * see the {@link Date) {@code os.date}} function.</p>
	 *
	 * <p>The values in these fields do not need to be inside their valid ranges. For instance,
	 * if {@code sec} is -10, it means -10 seconds from the time specified by the other fields;
	 * if {@code hour} is 1000, it means +1000 hours from the time specified by
	 * the other fields.</p>
	 *
	 * <p>The returned value is a number, whose meaning depends on your system. In POSIX, Windows,
	 * and some other systems, this number counts the number of seconds since some given start
	 * time (the "epoch"). In other systems, the meaning is not specified, and the number returned
	 * by {@code time} can be used only as an argument to {@link Date) {@code os.date}}
	 * and {@link DiffTime) {@code os.difftime}}.</p>
	 */
	static class Time extends UnimplementedFunction {
		// TODO
		public Time(RuntimeEnvironment runtimeEnvironment) {
			super("os.time");
		}
	}

	/**
	 * {@code os.tmpname ()}
	 *
	 * <p>Returns a string with a file name that can be used for a temporary file.
	 * The file must be explicitly opened before its use and explicitly removed when
	 * no longer needed.</p>
	 *
	 * <p>On POSIX systems, this function also creates a file with that name, to avoid security
	 * risks. (Someone else might create the file with wrong permissions in the time between
	 * getting the name and creating the file.) You still have to open the file to use it
	 * and to remove it (even if you do not use it).</p>
	 *
	 * <p>When possible, you may prefer to use {@code io.tmpfile},
	 * which automatically removes the file when the program ends.</p>
	 */
	static class TmpName extends UnimplementedFunction {
		// TODO
		public TmpName(RuntimeEnvironment runtimeEnvironment) {
			super("os.tmpname");
		}
	}
	
}
