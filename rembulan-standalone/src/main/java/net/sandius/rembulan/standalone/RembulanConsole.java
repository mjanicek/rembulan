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
import net.sandius.rembulan.compiler.ChunkClassLoader;
import net.sandius.rembulan.compiler.CompiledModule;
import net.sandius.rembulan.compiler.Compiler;
import net.sandius.rembulan.compiler.CompilerSettings;
import net.sandius.rembulan.core.Call;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Variable;
import net.sandius.rembulan.core.impl.DefaultLuaState;
import net.sandius.rembulan.lib.ModuleLib;
import net.sandius.rembulan.lib.impl.*;
import net.sandius.rembulan.parser.ParseException;
import net.sandius.rembulan.parser.Parser;
import net.sandius.rembulan.parser.TokenMgrError;
import net.sandius.rembulan.util.Check;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.nio.file.FileSystems;
import java.util.concurrent.ExecutionException;

public class RembulanConsole {

	private static final String VERSION = "0.1-SNAPSHOT";

	private static final String DEFAULT_PROMPT_1 = "> ";
	private static final String DEFAULT_PROMPT_2 = ">> ";

	private final CommandLineArguments config;

	private final InputStream in;
	private final PrintStream out;
	private final PrintStream err;

	private final LuaState state;
	private final Table env;

	private final Compiler compiler;
	private final ChunkClassLoader loader;

	private int chunkIndex;

	private final Function requireFunction;

	public RembulanConsole(CommandLineArguments cmdLineArgs, InputStream in, PrintStream out, PrintStream err) {

		this.config = Check.notNull(cmdLineArgs);

		this.in = Check.notNull(in);
		this.out = Check.notNull(out);
		this.err = Check.notNull(err);

		this.state = new DefaultLuaState();
		this.env = state.newTable();

		this.compiler = new Compiler(CompilerSettings.defaultSettings());
		this.loader = new ChunkClassLoader(this.getClass().getClassLoader());

		this.chunkIndex = 0;

		// install libraries
		new DefaultBasicLib(new PrintStream(out)).installInto(state, env);
		ModuleLib moduleLib = new DefaultModuleLib();
		moduleLib.installInto(state, env);
		new DefaultCoroutineLib().installInto(state, env);
		new DefaultStringLib().installInto(state, env);
		new DefaultMathLib().installInto(state, env);
		new DefaultTableLib().installInto(state, env);
		new DefaultIoLib(state.tableFactory(), FileSystems.getDefault(), in, out, err)
				.installInto(state, env);
		new DefaultOsLib().installInto(state, env);
		new DefaultUtf8Lib().installInto(state, env);
		new DefaultDebugLib().installInto(state, env);

		// command-line arguments
		env.rawset("arg", cmdLineArgs.toArgTable(state.tableFactory()));

		requireFunction = moduleLib._require();
	}

	private void printVersion() {
		out.println("Rembulan version " + VERSION + " (" + System.getProperty("java.vm.name") + ", Java " + System.getProperty("java.version") + ")");
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

	private CompiledModule parseProgram(String sourceText, String sourceFileName) throws ParseException {
		String rootClassName = "rembulan_repl_" + (chunkIndex++);
		return compiler.compile(
				sourceText,
				sourceFileName != null ? sourceFileName : "stdin",
				rootClassName);
	}

	private Function compileAndLoadProgram(String sourceText, String sourceFileName) throws ParseException, ReflectiveOperationException {
		CompiledModule cm = parseProgram(sourceText, sourceFileName);

		assert (cm != null);

		String mainClassName = loader.install(cm);
		Class<?> clazz = loader.loadClass(mainClassName);
		Constructor<?> constructor = clazz.getConstructor(Variable.class);
		Object instance = constructor.newInstance(new Variable(env));
		return (Function) instance;
	}

	private Object[] callFunction(Function fn, Object... args) throws ExecutionException, InterruptedException {
		Call call = Call.init(state, fn, args);
		Call.EventHandler handler = new Call.DefaultEventHandler();
		PreemptionContext preemptionContext = new PreemptionContext.Never();

		while (call.state() == Call.State.PAUSED) {
			call.resume(handler, preemptionContext);
		}

		return call.result().get();
	}

	private void executeProgram(String sourceText, String sourceFileName, String[] args)
			throws ParseException, ReflectiveOperationException, ExecutionException, InterruptedException {

		Check.notNull(sourceText);
		Check.notNull(sourceFileName);
		Check.notNull(args);

		Function fn = compileAndLoadProgram(sourceText, sourceFileName);

		Object[] callArgs = new Object[args.length];
		System.arraycopy(args, 0, callArgs, 0, args.length);

		callFunction(fn, callArgs);
	}

	private void executeFile(String fileName, String[] args)
			throws IOException, InterruptedException, ReflectiveOperationException, ExecutionException, ParseException {
		Check.notNull(fileName);
		executeProgram(Utils.skipLeadingShebang(Utils.readFile(fileName)), fileName, Check.notNull(args));
	}

	private void executeStdin(String[] args) throws InterruptedException, ReflectiveOperationException, ExecutionException, ParseException, IOException {
		executeProgram(Utils.skipLeadingShebang(Utils.readInputStream(in)), "stdin", Check.notNull(args));
	}

	private void requireModule(String moduleName) throws ExecutionException, InterruptedException {
		callFunction(requireFunction, Check.notNull(moduleName));
	}

	private void execute(Function fn) throws ExecutionException, InterruptedException {
		Object[] results = callFunction(fn);
		if (results.length > 0) {
			callFunction(new DefaultBasicLib.Print(out), results);
		}
	}

	public void start() throws Exception {
		for (CommandLineArguments.Step step : config.steps()) {
			executeStep(step);
		}

		if (config.interactive()) {
			startInteractive();
		}
	}

	private void executeStep(CommandLineArguments.Step s)
			throws InterruptedException, ReflectiveOperationException, ExecutionException, ParseException, IOException {

		Check.notNull(s);

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

	private String prompt1() {
		String s = Conversions.stringValueOf(env.rawget("_PROMPT"));
		return s != null ? s : DEFAULT_PROMPT_1;
	}

	private String prompt2() {
		String s = Conversions.stringValueOf(env.rawget("_PROMPT2"));
		return s != null ? s : DEFAULT_PROMPT_2;
	}

	private void startInteractive() throws IOException {
		ConsoleReader reader = new ConsoleReader(in, out);

		reader.setExpandEvents(false);

		String line;
		StringBuilder codeBuffer = new StringBuilder();
		boolean multiline = false;
		reader.setPrompt(prompt1());

		while ((line = reader.readLine()) != null) {
			out.print("");

			Function fn = null;

			try {
				if (!multiline) {
					try {
						fn = compileAndLoadProgram("return " + line, "stdin");
					}
					catch (ParseException ex) {
						// ignore
					}
				}

				if (fn == null) {
					codeBuffer.append(line).append('\n');
					try {
						fn = compileAndLoadProgram(codeBuffer.toString(), "stdin");
					}
					catch (TokenMgrError ex) {
						String msg = ex.getMessage();
						// TODO: is there really no better way?
						if (msg.contains("Encountered: <EOF>")) {
							// partial input
							reader.setPrompt(prompt2());
							multiline = true;
						}
						else {
							// faulty input
							out.println(msg);

							// reset back to initial state
							codeBuffer.setLength(0);
							multiline = false;
							reader.setPrompt(prompt1());
						}
					}
					catch (ParseException ex) {
						if (ex.currentToken != null
								&& ex.currentToken.next != null
								&& ex.currentToken.next.kind == Parser.EOF) {

							// partial input
							reader.setPrompt(prompt2());
							multiline = true;
						}
						else {
							// faulty input
							out.println(ex.getMessage());

							// reset back to initial state
							codeBuffer.setLength(0);
							multiline = false;
							reader.setPrompt(prompt1());
						}
					}
				}
			}
			catch (ReflectiveOperationException ex) {
				// this is a fatal error
				throw new RuntimeException(ex);
			}

			if (fn != null) {
				// reset back to initial state
				codeBuffer.setLength(0);
				multiline = false;

				try {
					execute(fn);
				}
				catch (ExecutionException ex) {
					// TODO: print a Lua stacktrace
					ex.printStackTrace(out);
				}
				catch (InterruptedException ex) {
					err.println("Interrupted");
				}

				reader.setPrompt(prompt1());
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

		try {
			console.start();
		}
		catch (Exception ex) {
			// TODO: print a Lua stacktrace
			ex.printStackTrace(System.err);
			System.exit(1);
		}

		System.exit(0);
	}

}
