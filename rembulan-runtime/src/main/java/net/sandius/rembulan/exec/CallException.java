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

package net.sandius.rembulan.exec;

import net.sandius.rembulan.load.ChunkClassLoader;

import java.io.PrintStream;
import java.util.Objects;

/**
 * An exception thrown to indicate an error in the execution of a Lua call.
 */
public class CallException extends Exception {

	/**
	 * Wraps the throwable {@code cause} into a {@code CallException}.
	 *
	 * <p>Note that in contrast to the standard {@code Exception} contract, {@code cause}
	 * must not be {@code null}.</p>
	 *
	 * @param cause  error cause, must not be {@code null}
	 */
	public CallException(Throwable cause) {
		super(Objects.requireNonNull(cause));
	}

	/**
	 * Prints a stack traceback formatted similarly to the PUC-Lua stack traceback.
	 *
	 * <p>This method builds the stack traceback from a Java stack trace, but attempts
	 * to filter out intermediate Java method calls that do not correspond to Lua function
	 * calls. In order to identify Lua function classes in the Java stack trace,
	 * it queries {@code chunkClassLoader} (since this information is not available
	 * in the stack trace).</p>
	 *
	 * <p>When non-{@code null}, the argument {@code suppress}, is used to define Java classes
	 * to be suppressed in the traceback in addition to the predefined runtime classes.</p>
	 *
	 * <p>The traceback is printed as relative to the point of execution of this method.</p>
	 *
	 * @param stream  print stream to use, must not be {@code null}
	 * @param chunkClassLoader  chunk class loader for determining whether classes correspond
	 *                          to Lua functions, must not be {@code null}
	 * @param suppress  additional suppressed classes, may be {@code null}
	 *
	 * @throws NullPointerException  if {@code stream} or {@code chunkClassLoader} is {@code null}
	 */
	public void printLuaFormatStackTraceback(
			PrintStream stream,
			ChunkClassLoader chunkClassLoader,
			String[] suppress) {

		Throwable cause = this.getCause();

		StackTraceback traceback = StackTraceback.getStackTraceback(
				cause, Thread.currentThread().getStackTrace(), chunkClassLoader, suppress);

		stream.println(cause.getMessage());
		stream.print(traceback.toString());
	}

}
