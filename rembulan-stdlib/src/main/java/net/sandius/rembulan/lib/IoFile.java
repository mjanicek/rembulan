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
import net.sandius.rembulan.Table;
import net.sandius.rembulan.impl.DefaultUserdata;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.io.IOException;

public abstract class IoFile extends DefaultUserdata {

	/**
	 * {@code file:close ()}
	 *
	 * <p>Closes {@code file}. Note that files are automatically closed when their handles
	 * are garbage collected, but that takes an unpredictable amount of time to happen.</p>
	 *
	 * <p>When closing a file handle created with {@code io.popen},
	 * {@code file:close} returns the same values returned by {@code os.execute}.</p>
	 */
	public static final LuaFunction CLOSE = new Close();

	/**
	 * {@code file:flush ()}
	 *
	 * <p>Saves any written data to {@code file}.</p>
	 */
	public static final LuaFunction FLUSH = new Flush();

	/**
	 * {@code file:lines (···)}
	 *
	 * <p>Returns an iterator function that, each time it is called, reads the file according
	 * to the given formats. When no format is given, uses "{@code l}" as a default.
	 * As an example, the construction</p>
	 *
	 * <pre>
	 * {@code
	 * for c in file:lines(1) do body end
	 * }
	 * </pre>
	 *
	 * <p>will iterate over all characters of the file, starting at the current position.
	 * Unlike {@code io.lines}, this function does not close the file
	 * when the loop ends.</p>
	 *
	 * <p>In case of errors this function raises the error, instead of returning an error code.</p>
	 */
	public static final LuaFunction LINES = new Lines();

	/**
	 * {@code file:read (···)}
	 *
	 * <p>Reads the file {@code file}, according to the given formats, which specify what to read.
	 * For each format, the function returns a string or a number with the characters read,
	 * or <b>nil</b> if it cannot read data with the specified format. (In this latter case,
	 * the function does not read subsequent formats.) When called without formats, it uses
	 * a default format that reads the next line (see below).</p>
	 *
	 * <p>The available formats are</p>
	 * <ul>
	 * <li><b>"{@code n}"</b>: reads a numeral and returns it as a float or an integer,
	 * following the lexical conventions of Lua. (The numeral may have leading spaces and a sign.)
	 * This format always reads the longest input sequence that is a valid prefix for a numeral;
	 * if that prefix does not form a valid numeral (e.g., an empty string, {@code "0x"},
	 * or {@code "3.4e-"}), it is discarded and the function returns <b>nil</b>.</li>
	 * <li><b>"{@code a}"</b>: reads the whole file, starting at the current position. On end of file,
	 * it returns the empty string.</li>
	 * <li><b>"{@code l}"</b>: reads the next line skipping the end of line, returning <b>nil</b>
	 * on end of file. This is the default format.</li>
	 * <li><b>"{@code L}"</b>: reads the next line keeping the end-of-line character (if present),
	 * returning <b>nil</b> on end of file.</li>
	 * <li><b><i>number</i></b>: reads a string with up to this number of bytes,
	 * returning <b>nil</b> on end of file. If <i>number</i> is zero, it reads nothing
	 * and returns an empty string, or <b>nil</b> on end of file.</li>
	 * </ul>
	 *
	 * <p>The formats "{@code l}" and "{@code L}" should be used only for text files.</p>
	 */
	public static final LuaFunction READ = new Read();

	/**
	 * {@code file:seek ([whence [, offset]])}
	 *
	 * <p>Sets and gets the file position, measured from the beginning of the file,
	 * to the position given by offset plus a base specified by the string {@code whence},
	 * as follows:</p>
	 * <ul>
	 * <li><b>{@code "set"}</b>: base is position 0 (beginning of the file);</li>
	 * <li><b>{@code "cur"}</b>: base is current position;</li>
	 * <li><b>{@code "end"}</b>: base is end of file;</li>
	 * </ul>
	 *
	 * <p>In case of success, {@code seek} returns the final file position, measured in bytes from
	 * the beginning of the file. If {@code seek} fails, it returns <b>nil</b>, plus a string
	 * describing the error.</p>
	 *
	 * <p>The default value for whence is {@code "cur"}, and for offset is 0. Therefore,
	 * the call {@code file:seek()} returns the current file position, without changing it;
	 * the call {@code file:seek("set")} sets the position to the beginning of the file
	 * (and returns 0); and the call {@code file:seek("end")} sets the position to the end
	 * of the file, and returns its size.</p>
	 */
	public static final LuaFunction SEEK = new Seek();

	/**
	 * {@code file:setvbuf (mode [, size])}
	 *
	 * <p>Sets the buffering mode for an output file. There are three available modes:</p>
	 * <ul>
	 * <li><b>{@code "no"}</b>: no buffering; the result of any output operation appears
	 * immediately.</li>
	 * <li><b>{@code "full"}</b>: full buffering; output operation is performed only when
	 * the buffer is full or when you explicitly flush the file
	 * (see {@code io.flush}).</li>
	 * <li><b>{@code "line"}</b>: line buffering; output is buffered until a newline is output
	 * or there is any input from some special files (such as a terminal device).</li>
	 * </ul>
	 * <p>For the last two cases, {@code size} specifies the size of the buffer, in bytes.
	 * The default is an appropriate size.</p>
	 */
	public static final LuaFunction SETVBUF = new SetVBuf();

	/**
	 * {@code file:write (···)}
	 *
	 * <p>Writes the value of each of its arguments to {@code file}. The arguments must be strings
	 * or numbers.</p>
	 *
	 * <p>In case of success, this function returns {@code file}. Otherwise it returns <b>nil</b>
	 * plus a string describing the error.</p>
	 */
	public static final LuaFunction WRITE = new Write();

	static final LuaFunction TOSTRING = new ToString();

	protected IoFile(Table metatable, Object userValue) {
		super(metatable, userValue);
	}

	public static String typeName() {
		return "FILE*";
	}

	@Override
	public String toString() {
		return "file (0x" + Integer.toHexString(hashCode()) + ")";
	}

	public abstract boolean isClosed();

	public abstract void close() throws IOException;

	public abstract void flush() throws IOException;

	public abstract void write(ByteString s) throws IOException;

	public enum Whence {
		BEGINNING,
		CURRENT_POSITION,
		END
	}

	public abstract long seek(Whence whence, long position) throws IOException;

	static class Close extends AbstractLibFunction {

		@Override
		protected String name() {
			return "close";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			final IoFile f = args.nextUserdata(typeName(), IoFile.class);

			try {
				f.close();
			}
			catch (Exception ex) {
				context.getReturnBuffer().setTo(null, ex.getMessage());
				return;
			}

			context.getReturnBuffer().setTo(true);
		}

	}

	static class Flush extends AbstractLibFunction {

		@Override
		protected String name() {
			return "flush";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			final IoFile f = args.nextUserdata(typeName(), IoFile.class);
			try {
				f.flush();
			}
			catch (Exception ex) {
				context.getReturnBuffer().setTo(null, ex.getMessage());
				return;
			}

			context.getReturnBuffer().setTo(true);
		}

	}

	static class Lines extends AbstractLibFunction {

		@Override
		protected String name() {
			return "lines";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			throw new UnsupportedOperationException();  // TODO
		}

	}

	static class Read extends AbstractLibFunction {

		@Override
		protected String name() {
			return "read";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			final IoFile f = args.nextUserdata(typeName(), IoFile.class);
			throw new UnsupportedOperationException();  // TODO
		}

	}

	static class Seek extends AbstractLibFunction {

		@Override
		protected String name() {
			return "seek";
		}

		private static Whence stringToWhence(String s) {
			switch (s) {
				case "set": return Whence.BEGINNING;
				case "cur": return Whence.CURRENT_POSITION;
				case "end": return Whence.END;
				default: return null;
			}
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			IoFile file = args.nextUserdata(typeName(), IoFile.class);

			final Whence whence;
			final long offset;

			if (args.hasNext()) {
				String s = args.nextString().toString();  // FIXME
				Whence w = stringToWhence(s);
				if (w == null) {
					throw args.badArgument(1, "invalid option '" + s + "'");
				}

				whence = w;
				offset = args.hasNext() ? args.nextInteger() : 0L;
			}
			else {
				whence = Whence.CURRENT_POSITION;
				offset = 0L;
			}

			final long position;
			try {
				position = file.seek(whence, offset);
			}
			catch (Exception ex) {
				context.getReturnBuffer().setTo(null, ex.getMessage());
				return;
			}

			context.getReturnBuffer().setTo(position);
		}

	}

	static class SetVBuf extends AbstractLibFunction {

		@Override
		protected String name() {
			return "setvbuf";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			throw new UnsupportedOperationException();  // TODO
		}

	}

	static class Write extends AbstractLibFunction {

		@Override
		protected String name() {
			return "write";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			final IoFile f = args.nextUserdata(typeName(), IoFile.class);
			while (args.hasNext()) {
				final ByteString s = args.nextString();
				try {
					f.write(s);
				}
				catch (Exception ex) {
					context.getReturnBuffer().setTo(null, ex.getMessage());
					return;
				}
			}

			context.getReturnBuffer().setTo(f);
		}

	}

	static class ToString extends AbstractLibFunction {

		@Override
		protected String name() {
			return "tostring";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			IoFile f = args.nextUserdata(typeName(), IoFile.class);
			context.getReturnBuffer().setTo(f.toString());
		}

	}

}

