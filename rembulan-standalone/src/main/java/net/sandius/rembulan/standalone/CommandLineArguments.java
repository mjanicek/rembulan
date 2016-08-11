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

package net.sandius.rembulan.standalone;

import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.List;

class CommandLineArguments {

	private final String[] args;
	private final int scriptIndex;

	private final boolean interactive;
	private final boolean ignoreEnvVars;

	private final Iterable<Step> steps;

	public CommandLineArguments(
			String[] args,
			int scriptIndex,
			boolean interactive,
			boolean ignoreEnvVars,
			Iterable<Step> steps) {

		this.args = Check.notNull(args);
		this.scriptIndex = scriptIndex;

		this.interactive = interactive;
		this.ignoreEnvVars = ignoreEnvVars;

		this.steps = Check.notNull(steps);
	}

	static class Step {

		enum What {
			PRINT_VERSION,
			EXECUTE_STRING,
			EXECUTE_FILE,
			EXECUTE_STDIN,
			REQUIRE_MODULE
		}

		private final What what;
		private final String arg0;
		private final String arg1;
		private final String[] argArray;

		private Step(What what, String arg0, String arg1, String[] argArray) {
			this.what = Check.notNull(what);
			this.arg0 = arg0;  // may be null
			this.arg1 = arg1;  // may be null
			this.argArray = argArray;  // may be null
		}

		public What what() {
			return what;
		}

		public String arg0() {
			return arg0;
		}

		public String arg1() {
			return arg1;
		}

		public String[] argArray() {
			return argArray;
		}

		public static Step printVersion() {
			return new Step(What.PRINT_VERSION, null, null, null);
		}

		public static Step executeString(String program, String origin) {
			return new Step(What.EXECUTE_STRING, Check.notNull(program), origin, null);
		}

		public static Step executeFile(String fileName, String[] arguments) {
			return new Step(What.EXECUTE_FILE, Check.notNull(fileName), null, Check.notNull(arguments));
		}

		public static Step executeStdin(String[] arguments) {
			return new Step(What.EXECUTE_STDIN, null, null, Check.notNull(arguments));
		}

		public static Step require(String moduleName) {
			return new Step(What.REQUIRE_MODULE, Check.notNull(moduleName), null, null);
		}

	}

	public boolean interactive() {
		return interactive;
	}

	public boolean ignoreEnvVars() {
		return ignoreEnvVars();
	}

	public Iterable<Step> steps() {
		return steps;
	}

	public Table toArgTable(TableFactory tableFactory) {
		Table t = tableFactory.newTable();

		// Caveat: does not insert the interpreter name
		// scriptIndex == -1 when no script was specified

		for (int i = 0; i < args.length; i++) {
			t.rawset(i - scriptIndex, args[i]);
		}

		return t;
	}

	public static CommandLineArguments parseArguments(String[] args, boolean inTty) {
		Check.notNull(args);

		boolean explicitPrintVersion = false;
		boolean interactive = false;
		boolean explicitIgnoreEnvVars = false;

		boolean stdin = false;

		List<Step> steps = new ArrayList<>();

		int i = 0;

		// handle options

		for ( ; i < args.length; i++) {
			String arg = Check.notNull(args[i]);

			// -v       show version information
			if (arg.equals("-v")) {
				explicitPrintVersion = true;
				continue;
			}

			// -i       enter interactive mode after executing 'script'
			else if (arg.equals("-i")) {
				interactive = true;
				continue;
			}

			// -e stat  execute string 'stat'
			else if (arg.startsWith("-e")) {
				String suffix = arg.substring(2);
				final Step step;

				if (suffix.isEmpty()) {
					// statement is in the next argument
					if (i + 1 < args.length) {
						step = Step.executeString(args[++i], "(command line)");
					}
					else {
						throw new IllegalArgumentException("'-e' needs argument");
					}
				}
				else {
					// suffix is the statement
					step = Step.executeString(suffix, "(command line)");
				}

				steps.add(step);
				continue;
			}

			// -l name  require library 'name'
			else if (arg.startsWith("-l")) {
				String suffix = arg.substring(2);
				final Step step;

				if (suffix.isEmpty()) {
					// module name is in the next argument
					if (i + 1 < args.length) {
						step = Step.require(args[++i]);
					}
					else {
						throw new IllegalArgumentException("'-l' needs argument");
					}
				}
				else {
					// suffix is the module name
					step = Step.require(suffix);
				}

				steps.add(step);
				continue;
			}

			// -E       ignore environment variables
			else if (arg.equals("-E")) {
				explicitIgnoreEnvVars = true;
				continue;
			}

			// --       stop handling options
			else if (arg.equals("--")) {
				i += 1;
				break;
			}

			// -        stop handling options and execute stdin
			else if (arg.equals("-")) {
				stdin = true;
				i += 1;
				break;
			}

			else if (arg.startsWith("-")) {
				throw new IllegalArgumentException("unrecognized option '" + arg + "'");
			}

			else {
				// this not an option
				break;
			}
		}

		// handle script and its arguments
		int scriptIndex = -1;
		{
			String script = null;

			if (!stdin) {
				if (i < args.length) {
					scriptIndex = i++;
					script = args[scriptIndex];
				}
			}

			// script arguments
			final String[] scriptArgs;
			{
				List<String> tmp = new ArrayList<>();
				while (i < args.length) {
					tmp.add(args[i++]);
				}
				scriptArgs = tmp.toArray(new String[tmp.size()]);
			}

			if (stdin) {
				steps.add(Step.executeStdin(scriptArgs));
			}
			else if (script != null) {
				steps.add(Step.executeFile(script, scriptArgs));
			}
		}

		/*
		 * Note down whether we've already inserted a step for printing the version string.
		 * This is not very nice, but it allows us to mimic the following behaviour
		 * of PUC-Lua 5.3.3:
		 *
		 * $ LUA_INIT='print(123)' lua
		 * -->
		 * 123
		 * Lua 5.3.3  Copyright (C) 1994-2016 Lua.org, PUC-Rio
		 * > (interactive mode)
		 *
		 * vs:
		 *
		 * $ LUA_INIT='print(123)' lua -i -v
		 * -->
		 * Lua 5.3.3  Copyright (C) 1994-2016 Lua.org, PUC-Rio
		 * 123
		 * > (interactive mode)
		 */

		boolean versionPrinted = false;

		// fall back to defaults when no arguments are specified
		if (args.length == 0) {
			// no arguments
			if (inTty) {
				// force -i -v
				// if LUA_INIT_x is used, this will be executed *before* the version is printed
				steps.add(Step.printVersion());
				versionPrinted = true;
				interactive = true;
			}
			else {
				// force -
				steps.add(Step.executeStdin(new String[0]));
			}
		}

		// check LUA_INIT_5_3 or LUA_INIT
		if (!explicitIgnoreEnvVars) {
			String origin = "LUA_INIT_5_3";
			String init = System.getenv(origin);
			if (init == null) {
				origin = "LUA_INIT";
				init = System.getenv(origin);
			}

			if (init != null) {
				Step step = init.startsWith("@")
						? Step.executeFile(init.substring(1), new String[0])
						: Step.executeString(init, origin);

				steps.add(0, step);
			}
		}

		// were we explicitly asked to print the version string?
		if (!versionPrinted && (explicitPrintVersion || interactive)) {
			steps.add(0, Step.printVersion());
		}

		return new CommandLineArguments(
				args,
				scriptIndex,
				interactive,
				explicitIgnoreEnvVars,
				steps);
	}

}
