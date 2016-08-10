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
import net.sandius.rembulan.lib.impl.DefaultBasicLib;
import net.sandius.rembulan.lib.impl.DefaultCoroutineLib;
import net.sandius.rembulan.lib.impl.DefaultDebugLib;
import net.sandius.rembulan.lib.impl.DefaultIOLib;
import net.sandius.rembulan.lib.impl.DefaultMathLib;
import net.sandius.rembulan.lib.impl.DefaultStringLib;
import net.sandius.rembulan.lib.impl.DefaultTableLib;
import net.sandius.rembulan.parser.ParseException;
import net.sandius.rembulan.parser.Parser;
import net.sandius.rembulan.util.Check;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.nio.file.FileSystems;
import java.util.concurrent.ExecutionException;

public class RembulanConsole {

	private static final String VERSION = "0.1-SNAPSHOT";

	private static final String PROMPT_1 = "> ";
	private static final String PROMPT_2 = ">> ";

	private final PrintStream out;

	private final LuaState state;
	private final Table env;

	private final Compiler compiler;
	private final ChunkClassLoader loader;

	private int chunkIndex;

	public RembulanConsole(InputStream in, PrintStream out, PrintStream err, String[] args) {
		this.out = Check.notNull(out);

		this.state = new DefaultLuaState();
		this.env = state.newTable();

		this.compiler = new Compiler(CompilerSettings.defaultSettings());
		this.loader = new ChunkClassLoader(this.getClass().getClassLoader());

		this.chunkIndex = 0;

		installLibraries(in, out, err);
		env.rawset("arg", argsTable(args));
	}

	private void installLibraries(InputStream in, OutputStream out, OutputStream err) {
		new DefaultBasicLib(new PrintStream(out)).installInto(state, env);

		Table coroutineLibTable = state.newTable();
		new DefaultCoroutineLib().installInto(state, coroutineLibTable);
		env.rawset("coroutine", coroutineLibTable);

		Table stringLibTable = state.newTable();
		new DefaultStringLib().installInto(state, stringLibTable);
		env.rawset("string", stringLibTable);

		Table mathLibTable = state.newTable();
		new DefaultMathLib().installInto(state, mathLibTable);
		env.rawset("math", mathLibTable);

		Table tableLibTable = state.newTable();
		new DefaultTableLib().installInto(state, tableLibTable);
		env.rawset("table", tableLibTable);

		Table ioLibTable = state.newTable();
		new DefaultIOLib(state.tableFactory(), FileSystems.getDefault(), in, out, err)
				.installInto(state, ioLibTable);
		env.rawset("io", ioLibTable);

		Table debugLibTable = state.newTable();
		new DefaultDebugLib().installInto(state, debugLibTable);
		env.rawset("debug", debugLibTable);
	}

	private Table argsTable(String[] args) {
		Table t = state.newTable();
		for (int i = 0; i < args.length; i++) {
			t.rawset(i + 1, args[i]);
		}
		return t;
	}

	private CompiledModule parseProgram(String source) throws ParseException {
		return compiler.compile(source, "stdin", "stdin_" + chunkIndex++);
	}

	private Function processLine(String line) throws ParseException, ReflectiveOperationException {
		CompiledModule cm = parseProgram(line);

		assert (cm != null);

		String mainClassName = loader.install(cm);
		Class<?> clazz = loader.loadClass(mainClassName);
		Constructor<?> constructor = clazz.getConstructor(Variable.class);
		Object instance = constructor.newInstance(new Variable(env));
		return (Function) instance;
	}

	private void execute(Function fn) throws ExecutionException, InterruptedException {
		Call call = Call.init(state, fn);
		Call.EventHandler handler = new Call.DefaultEventHandler();
		PreemptionContext preemptionContext = new PreemptionContext.Never();

		while (call.state() == Call.State.PAUSED) {
			call.resume(handler, preemptionContext);
		}

		Object[] results = call.result().get();
		if (results.length > 0) {
			printResults(results);
		}
	}

	private void printResults(Object[] results) {
		for (int i = 0; i < results.length; i++) {
			out.print(Conversions.toHumanReadableString(results[i]));
			if (i + 1 < results.length) {
				out.print('\t');
			}
		}
		out.println();
	}

	public static void main(String[] args) throws IOException {
		InputStream in = System.in;
		PrintStream out = System.out;
		PrintStream err = System.err;

		ConsoleReader reader = new ConsoleReader(in, out);
		RembulanConsole repl = new RembulanConsole(in, out, err, args);

		out.println("Rembulan version " + VERSION + " (" + System.getProperty("java.vm.name") + ", Java " + System.getProperty("java.version") + ")");

		reader.setExpandEvents(false);

		String line;
		StringBuilder codeBuffer = new StringBuilder();
		boolean multiline = false;
		reader.setPrompt(PROMPT_1);

		while ((line = reader.readLine()) != null) {
			out.print("");

			Function fn = null;

			try {
				if (!multiline) {
					try {
						fn = repl.processLine("return " + line);
					}
					catch (ParseException ex) {
						// ignore
					}
				}

				if (fn == null) {
					codeBuffer.append(line).append('\n');
					try {
						fn = repl.processLine(codeBuffer.toString());
					}
					catch (ParseException ex) {
						if (ex.currentToken != null
								&& ex.currentToken.next != null
								&& ex.currentToken.next.kind == Parser.EOF) {

							// partial input
							reader.setPrompt(PROMPT_2);
							multiline = true;
						}
						else {
							// faulty input
							out.println(ex.getMessage());

							// reset back to initial state
							codeBuffer.setLength(0);
							multiline = false;
							reader.setPrompt(PROMPT_1);
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
				reader.setPrompt(PROMPT_1);

				try {
					repl.execute(fn);
				}
				catch (ExecutionException ex) {
					ex.printStackTrace(out);
				}
				catch (InterruptedException ex) {
					out.println("Interrupted");
				}
			}

		}
	}

}
