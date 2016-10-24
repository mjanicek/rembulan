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

import jline.console.ConsoleReader;
import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.CompilerChunkLoader;
import net.sandius.rembulan.compiler.CompilerSettings;
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.env.RuntimeEnvironments;
import net.sandius.rembulan.exec.CallException;
import net.sandius.rembulan.exec.CallPausedException;
import net.sandius.rembulan.exec.DirectCallExecutor;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.lib.StandardLibrary;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.runtime.LuaFunction;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;

public class RembulanConsole {

	private final CommandLineArguments config;

	private final InputStream in;
	private final PrintStream out;
	private final PrintStream err;

	private final StateContext state;
	private final Table env;

	private final CompilerChunkLoader loader;

	private int chunkIndex;

	private final LuaFunction printFunction;
	private final LuaFunction requireFunction;

	private final boolean javaTraceback;
	private final boolean stackTraceForCompileErrors;
	private final String[] tracebackSuppress;

	private final DirectCallExecutor callExecutor;

	public RembulanConsole(CommandLineArguments cmdLineArgs, InputStream in, PrintStream out, PrintStream err) {

		javaTraceback = System.getenv(Constants.ENV_FULL_TRACEBACK) != null;
		tracebackSuppress = new String[] {
				"net.sandius.rembulan.core",
				this.getClass().getName()
		};
		stackTraceForCompileErrors = javaTraceback;

		this.config = Objects.requireNonNull(cmdLineArgs);

		this.in = Objects.requireNonNull(in);
		this.out = Objects.requireNonNull(out);
		this.err = Objects.requireNonNull(err);

		CompilerSettings.CPUAccountingMode cpuAccountingMode =
				System.getenv(Constants.ENV_CPU_ACCOUNTING) != null
				? CompilerSettings.CPUAccountingMode.IN_EVERY_BASIC_BLOCK
				: CompilerSettings.CPUAccountingMode.NO_CPU_ACCOUNTING;
		CompilerSettings compilerSettings = CompilerSettings
				.defaultSettings()
				.withCPUAccountingMode(cpuAccountingMode);

		this.state = StateContexts.newDefaultInstance();
		this.loader = CompilerChunkLoader.of(compilerSettings, "rembulan_repl_");
		RuntimeEnvironment runtimeEnv = RuntimeEnvironments.system(in, out, err);
		this.env = StandardLibrary.in(runtimeEnv)
				.withLoader(loader)
				.withDebug(true)
				.installInto(state);

		printFunction = Aux.callGlobal(env, "print");
		requireFunction = Aux.callGlobal(env, "require");

		this.callExecutor = DirectCallExecutor.newExecutor();

		// command-line arguments
		env.rawset("arg", cmdLineArgs.toArgTable(state));

	}

	private void printVersion() {
		out.println("Rembulan " + Constants.VERSION
				+ " ("
				+ System.getProperty("java.vm.name")
				+ ", Java "
				+ System.getProperty("java.version")
				+ ")");
	}

	private static void printUsage(PrintStream out) {
		String programName = "rembulan";

		out.println("usage: " + programName + " [options] [script [args]]");
		out.println("Available options are:");
		out.println("  -e stat  execute string 'stat'");
		out.println("  -i       enter interactive mode after executing 'script'");
		out.println("  -l name  require library 'name'");
		out.println("  -v       show version information");
		out.println("  -E       ignore environment variables");
		out.println("  --       stop handling options");
		out.println("  -        stop handling options and execute stdin");
	}

	private Object[] callFunction(LuaFunction fn, Object... args)
			throws CallException {

		try {
			return callExecutor.call(state, fn, args);
		}
		catch (CallPausedException | InterruptedException ex) {
			throw new CallException(ex);
		}
	}

	private void executeProgram(String sourceText, String sourceFileName, String[] args)
			throws LoaderException, CallException {

		Objects.requireNonNull(sourceText);
		Objects.requireNonNull(sourceFileName);
		Objects.requireNonNull(args);

		LuaFunction fn = loader.loadTextChunk(new Variable(env), sourceFileName, sourceText);

		Object[] callArgs = new Object[args.length];
		System.arraycopy(args, 0, callArgs, 0, args.length);

		callFunction(fn, callArgs);
	}

	private void executeFile(String fileName, String[] args)
			throws LoaderException, CallException  {

		Objects.requireNonNull(fileName);
		final String source;
		try {
			source = Utils.readFile(fileName);
		}
		catch (IOException ex) {
			throw new LoaderException(ex, fileName);
		}

		executeProgram(Utils.skipLeadingShebang(source), fileName, Objects.requireNonNull(args));
	}

	private void executeStdin(String[] args)
			throws LoaderException, CallException  {

		final String source;
		try {
			source = Utils.readInputStream(in);
		}
		catch (IOException ex) {
			throw new LoaderException(ex, Constants.SOURCE_STDIN);
		}

		executeProgram(Utils.skipLeadingShebang(source), Constants.SOURCE_STDIN, Objects.requireNonNull(args));
	}

	private void requireModule(String moduleName) throws CallException {
		callFunction(requireFunction, Objects.requireNonNull(moduleName));
	}

	private void execute(LuaFunction fn) throws CallException {
		Object[] results = callFunction(fn);
		if (results.length > 0) {
			callFunction(printFunction, results);
		}
	}

	public boolean start() throws CallException, IOException {
		try {
			for (CommandLineArguments.Step step : config.steps()) {
				executeStep(step);
			}
		}
		catch (CallException ex) {
			if (!javaTraceback) {
				ex.printLuaFormatStackTraceback(err, loader.getChunkClassLoader(), tracebackSuppress);
			}
			else {
				ex.printStackTrace(err);
			}
			return false;
		}
		catch (LoaderException ex) {
			if (!stackTraceForCompileErrors) {
				err.println(ex.getLuaStyleErrorMessage());
			}
			else {
				ex.printStackTrace(err);
			}
			return false;
		}

		if (config.interactive()) {
			startInteractive();
		}

		return true;
	}

	private void executeStep(CommandLineArguments.Step s)
			throws LoaderException, CallException  {

		Objects.requireNonNull(s);

		switch (s.what()) {

			case PRINT_VERSION:
				printVersion();
				break;

			case EXECUTE_STRING:
				executeProgram(s.arg0(), s.arg1(), new String[0]);
				break;

			case EXECUTE_FILE:
				executeFile(s.arg0(), s.argArray());
				break;

			case EXECUTE_STDIN:
				executeStdin(s.argArray());
				break;

			case REQUIRE_MODULE:
				requireModule(s.arg0());
				break;

		}
	}

	private String getGlobalString(String name, String defaultValue) throws CallException {
		final Object[] result;
		result = callFunction(Aux.index(env, name));

		if (result.length > 0) {
			ByteString s = Conversions.stringValueOf(result[0]);
			return s != null ? s.toString() : defaultValue;
		}
		else {
			return defaultValue;
		}
	}

	private String getPrompt() throws CallException {
		return getGlobalString(Constants.VAR_NAME_PROMPT, Constants.DEFAULT_PROMPT);
	}

	private String getPrompt2() throws CallException {
		return getGlobalString(Constants.VAR_NAME_PROMPT2, Constants.DEFAULT_PROMPT2);
	}

	private void startInteractive() throws CallException, IOException {
		ConsoleReader reader = new ConsoleReader(in, out);

		reader.setExpandEvents(false);

		String line;
		StringBuilder codeBuffer = new StringBuilder();
		reader.setPrompt(getPrompt());

		while ((line = reader.readLine()) != null) {
			out.print("");

			LuaFunction fn = null;

			boolean firstLine = codeBuffer.length() == 0;
			boolean emptyInput = line.trim().isEmpty();

			if (firstLine && !emptyInput) {
				try {
					fn = loader.loadTextChunk(new Variable(env), Constants.SOURCE_STDIN, "return " + line);
				}
				catch (LoaderException ex) {
					// ignore
				}
			}

			if (fn == null) {
				codeBuffer.append(line).append('\n');
				try {
					fn = loader.loadTextChunk(new Variable(env), Constants.SOURCE_STDIN, codeBuffer.toString());
				}
				catch (LoaderException ex) {
					if (ex.isPartialInputError()) {
						// partial input
						reader.setPrompt(getPrompt2());
					}
					else {
						// faulty input
						if (!stackTraceForCompileErrors) {
							err.println(ex.getLuaStyleErrorMessage());
						}
						else {
							ex.printStackTrace(err);
						}

						// reset back to initial state
						codeBuffer.setLength(0);
						reader.setPrompt(getPrompt());
					}
				}
			}

			if (fn != null) {
				// reset back to initial state
				codeBuffer.setLength(0);

				Object[] results = null;
				try {
					results = callFunction(fn);
				}
				catch (CallException ex) {
					if (!javaTraceback) {
						ex.printLuaFormatStackTraceback(err, loader.getChunkClassLoader(), tracebackSuppress);
					}
					else {
						ex.printStackTrace(err);
					}
				}

				if (results != null && results.length > 0) {
					try {
						callFunction(printFunction, results);
					}
					catch (CallException ex) {
						err.println("error calling 'print' ("
								+ Conversions.toErrorMessage(ex.getCause())
								+ ")");
					}
				}

				reader.setPrompt(getPrompt());
			}
		}
	}

	public static void main(String[] args) {
		// Caveat: inTty == true iff stdin *and* stdout are tty; however we only care about stdin
		boolean inTty = System.console() != null;

		CommandLineArguments cmdLineArgs = null;
		try {
			cmdLineArgs = CommandLineArguments.parseArguments(args, inTty);
		}
		catch (IllegalArgumentException ex) {
			System.err.println(ex.getMessage());
			printUsage(System.err);
			System.exit(1);
		}

		assert (cmdLineArgs != null);

		RembulanConsole console = new RembulanConsole(cmdLineArgs, System.in, System.out, System.err);

		int rc;
		try {
			rc = console.start() ? 1 : 0;
		}
		catch (CallException ex) {
			// error while retrieving _PROMPT or _PROMPT2
			System.err.println(ex.getCause().getMessage());
			rc = 1;
		}
		catch (Exception ex) {
			System.err.println("Encountered fatal error (aborting):");
			ex.printStackTrace(System.err);
			rc = 1;
		}

		System.exit(rc);
	}

}
