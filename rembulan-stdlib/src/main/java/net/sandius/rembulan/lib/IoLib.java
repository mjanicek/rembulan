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
 *
 * --
 * Portions of this file are licensed under the Lua license. For Lua
 * licensing details, please visit
 *
 *     http://www.lua.org/license.html
 *
 * Copyright (C) 1994-2016 Lua.org, PUC-Rio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.sandius.rembulan.lib;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Metatables;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.TableFactory;
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.io.InputStreamIoFile;
import net.sandius.rembulan.lib.io.OutputStreamIoFile;
import net.sandius.rembulan.runtime.Dispatch;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.UnresolvedControlThrowable;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * <p>The I/O library provides two different styles for file manipulation. The first one uses
 * implicit file handles; that is, there are operations to set a default input file and
 * a default output file, and all input/output operations are over these default files.
 * The second style uses explicit file handles.</p>
 *
 * <p>When using implicit file handles, all operations are supplied by table {@code io}.
 * When using explicit file handles, the operation {@code io.open}
 * returns a file handle and then all operations are supplied as methods of the file handle.</p>
 *
 * <p>The table {@code io} also provides three predefined file handles with their usual
 * meanings from C: {@code io.stdin}, {@code io.stdout}, and {@code io.stderr}.
 * The I/O library never closes these files.</p>
 *
 * Unless otherwise stated, all I/O functions return <b>nil</b> on failure (plus an error
 * message as a second result and a system-dependent error code as a third result) and some
 * value different from <b>nil</b> on success. On non-POSIX systems, the computation
 * of the error message and error code in case of errors may be not thread safe, because they
 * rely on the global C variable {@code errno}.
 */
public final class IoLib {

	static final LuaFunction TYPE = new Type();

	static final LuaFunction FILE_CLOSE = new IoFile.Close();
	static final LuaFunction FILE_FLUSH = new IoFile.Flush();
	static final LuaFunction FILE_LINES = new IoFile.Lines();
	static final LuaFunction FILE_READ = new IoFile.Read();
	static final LuaFunction FILE_SEEK = new IoFile.Seek();
	static final LuaFunction FILE_SETVBUF = new IoFile.SetVBuf();
	static final LuaFunction FILE_TOSTRING = new IoFile.ToString();
	static final LuaFunction FILE_WRITE = new IoFile.Write();

	/**
	 * Returns the function {@code io.type}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code io.type (obj)}
	 *
	 * <p>Checks whether {@code obj} is a valid file handle. Returns the string {@code "file"}
	 * if {@code obj} is an open file handle, {@code "closed file"} if {@code obj} is
	 * a closed file handle, or <b>nil</b> if {@code obj} is not a file handle.</p>
	 * </blockquote>
	 *
	 * @return  the {@code io.type} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-io.type">
	 *     the Lua 5.3 Reference Manual entry for <code>io.type</code></a>
	 */
	public static LuaFunction type() {
		return TYPE;
	}

	/**
	 * Returns the file method (a function) {@code close}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code file:close ()}
	 *
	 * <p>Closes {@code file}. Note that files are automatically closed when their handles
	 * are garbage collected, but that takes an unpredictable amount of time to happen.</p>
	 *
	 * <p>When closing a file handle created with {@code io.popen},
	 * {@code file:close} returns the same values returned by {@code os.execute}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code file:close} method
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-file:close">
	 *     the Lua 5.3 Reference Manual entry for <code>file:close</code></a>
	 */
	public static LuaFunction file_close() {
		return FILE_CLOSE;
	}

	/**
	 * Returns the file method (a function) {@code flush}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code file:flush ()}
	 *
	 * <p>Saves any written data to {@code file}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code file:flush} method
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-file:flush">
	 *     the Lua 5.3 Reference Manual entry for <code>file:flush</code></a>
	 */
	public static LuaFunction file_flush() {
		return FILE_FLUSH;
	}

	/**
	 * Returns the file method (a function) {@code lines}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
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
	 * </blockquote>
	 *
	 * @return  the {@code file:lines} method
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-file:lines">
	 *     the Lua 5.3 Reference Manual entry for <code>file:lines</code></a>
	 */
	public static LuaFunction file_lines() {
		return FILE_LINES;
	}

	/**
	 * Returns the file method (a function) {@code read}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
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
	 * </blockquote>
	 *
	 * @return  the {@code file:read} method
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-file:read">
	 *     the Lua 5.3 Reference Manual entry for <code>file:read</code></a>
	 */
	public static LuaFunction file_read() {
		return FILE_READ;
	}

	/**
	 * Returns the file method (a function) {@code seek}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
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
	 * </blockquote>
	 *
	 * @return  the {@code file:seek} method
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-file:seek">
	 *     the Lua 5.3 Reference Manual entry for <code>file:seek</code></a>
	 */
	public static LuaFunction file_seek() {
		return FILE_SEEK;
	}

	/**
	 * Returns the file method (a function) {@code setvbuf}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
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
	 * </blockquote>
	 *
	 * @return  the {@code file:setvbuf} method
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-file:setvbuf">
	 *     the Lua 5.3 Reference Manual entry for <code>file:setvbuf</code></a>
	 */
	public static LuaFunction file_setvbuf() {
		return FILE_SETVBUF;
	}

	/**
	 * Returns the file method (a function) {@code tostring}.
	 *
	 * @return  the {@code file:tostring} method
	 */
	public static LuaFunction file_tostring() {
		return FILE_TOSTRING;
	}

	/**
	 * Returns the file method (a function) {@code write}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code file:write (···)}
	 *
	 * <p>Writes the value of each of its arguments to {@code file}. The arguments must be strings
	 * or numbers.</p>
	 *
	 * <p>In case of success, this function returns {@code file}. Otherwise it returns <b>nil</b>
	 * plus a string describing the error.</p>
	 * </blockquote>
	 *
	 * @return  the {@code file:write} method
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-file:write">
	 *     the Lua 5.3 Reference Manual entry for <code>file:write</code></a>
	 */
	public static LuaFunction file_write() {
		return FILE_WRITE;
	}


	private final LuaFunction _close;
	private final LuaFunction _flush;
	private final LuaFunction _input;
	private final LuaFunction _lines;
	private final LuaFunction _open;
	private final LuaFunction _output;
	private final LuaFunction _popen;
	private final LuaFunction _read;
	private final LuaFunction _tmpfile;
	private final LuaFunction _write;

	private final Table fileMetatable;

	private final FileSystem fileSystem;

	private final IoFile stdIn;
	private final IoFile stdOut;
	private final IoFile stdErr;

	private IoFile defaultInput;
	private IoFile defaultOutput;

	private IoLib(
			TableFactory tableFactory,
			FileSystem fileSystem,
			InputStream in,
			OutputStream out,
			OutputStream err) {

		Objects.requireNonNull(tableFactory);

		// set up metatable for files
		Table mt = tableFactory.newTable();
		this.fileMetatable = mt;

		mt.rawset(Metatables.MT_INDEX, mt);
		mt.rawset(BasicLib.MT_NAME, IoFile.typeName());
		mt.rawset(BasicLib.MT_TOSTRING, file_tostring());
		// TODO: set the __gc metamethod
		mt.rawset("close", file_close());
		mt.rawset("flush", file_flush());
		mt.rawset("lines", file_lines());
		mt.rawset("read", file_read());
		mt.rawset("seek", file_seek());
		mt.rawset("setvbuf", file_setvbuf());
		mt.rawset("write", file_write());

		this.fileSystem = fileSystem;

		stdIn = in != null ? new InputStreamIoFile(in, mt, null) : null;
		stdOut = out != null ? new OutputStreamIoFile(out, mt, null) : null;
		stdErr = err != null ? new OutputStreamIoFile(err, mt, null) : null;

		defaultInput = stdIn;
		defaultOutput = stdOut;

		this._close = new Close(this);
		this._flush = new Flush(this);
		this._input = new Input(this);
		this._lines = new Lines(this);
		this._open = new Open(this);
		this._output = new Output(this);
		this._popen = new POpen(this);
		this._read = new Read(this);
		this._tmpfile = new TmpFile(this);
		this._write = new Write(this);
	}

	/**
	 * Installs the I/O library to the global environment {@code env} in the state
	 * context {@code context}. The I/O functions will use the runtime environment
	 * {@code runtimeEnvironment}.
	 *
	 * @param context  the state context, must not be {@code null}
	 * @param env  the global environment, must not be {@code null}
	 * @param runtimeEnvironment  the runtime environment, may be {@code null}
	 *
	 * @throws NullPointerException  if {@code context} or {@code env} is {@code null}
	 */
	public static void installInto(StateContext context, Table env, RuntimeEnvironment runtimeEnvironment) {
		Objects.requireNonNull(context);
		Objects.requireNonNull(env);

		Table t = context.newTable();

		FileSystem fileSystem = runtimeEnvironment != null ? runtimeEnvironment.fileSystem() : null;
		InputStream in = runtimeEnvironment != null ? runtimeEnvironment.standardInput() : null;
		OutputStream out = runtimeEnvironment != null ? runtimeEnvironment.standardOutput() : null;
		OutputStream err = runtimeEnvironment != null ? runtimeEnvironment.standardError() : null;

		IoLib l = new IoLib(context, fileSystem, in, out, err);

		t.rawset("close", l._close);
		t.rawset("flush", l._flush);
		t.rawset("input", l._input);
		t.rawset("lines", l._lines);
		t.rawset("open", l._open);
		t.rawset("output", l._output);
		t.rawset("popen", l._popen);
		t.rawset("read", l._read);
		t.rawset("tmpfile", l._tmpfile);
		t.rawset("type", type());
		t.rawset("write", l._write);

		t.rawset("stdin", l.stdIn);
		t.rawset("stdout", l.stdOut);
		t.rawset("stderr", l.stdErr);

		ModuleLib.install(env, "io", t);
	}


	private IoFile openFile(ByteString filename, Open.Mode mode) {
		Objects.requireNonNull(filename);
		Objects.requireNonNull(mode);

		if (fileSystem == null) {
			throw new UnsupportedOperationException("no filesystem");
		}

		Path path = fileSystem.getPath(filename.toString());

		throw new UnsupportedOperationException("open file");  // TODO
	}

	private IoFile setDefaultInputFile(IoFile f) {
		defaultInput = Objects.requireNonNull(f);
		return f;
	}

	private IoFile getDefaultInputFile() {
		return defaultInput;
	}

	private IoFile setDefaultOutputFile(IoFile f) {
		defaultOutput = Objects.requireNonNull(f);
		return f;
	}

	private IoFile getDefaultOutputFile() {
		return defaultOutput;
	}

	static class Close extends AbstractLibFunction {

		private final IoLib lib;

		public Close(IoLib lib) {
			this.lib = Objects.requireNonNull(lib);
		}

		@Override
		protected String name() {
			return "close";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			final IoFile file = args.hasNext()
					? args.nextUserdata(IoFile.typeName(), IoFile.class)
					: lib.getDefaultOutputFile();

			try {
				Dispatch.call(context, file_close(), file);
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// results already on stack, this is a no-op
		}

	}

	static class Flush extends AbstractLibFunction {

		private final IoLib lib;

		public Flush(IoLib lib) {
			this.lib = Objects.requireNonNull(lib);
		}

		@Override
		protected String name() {
			return "flush";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			IoFile outFile = lib.getDefaultOutputFile();

			try {
				Dispatch.call(context, file_flush());
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, outFile);
			}

			resume(context, outFile);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// results are already on the stack, this is a no-op
		}

	}

	static class Input extends AbstractLibFunction {

		private final IoLib lib;

		public Input(IoLib lib) {
			this.lib = Objects.requireNonNull(lib);
		}

		@Override
		protected String name() {
			return "input";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			if (args.hasNext()) {
				// open the argument for reading and set it as the default input file
				ByteString filename = args.nextString();
				IoFile f = lib.openFile(filename, Open.Mode.READ);
				assert (f != null);
				lib.setDefaultInputFile(f);
				context.getReturnBuffer().setTo(f);
			}
			else {
				// return the default input file
				IoFile inFile = lib.getDefaultInputFile();
				context.getReturnBuffer().setTo(inFile);
			}
		}

	}

	static class Lines extends UnimplementedFunction {
		// TODO
		public Lines(IoLib lib) {
			super("io.lines");
		}
	}

	static class Open extends AbstractLibFunction {

		private final IoLib lib;

		public Open(IoLib lib) {
			this.lib = Objects.requireNonNull(lib);
		}

		enum Mode {
			READ,
			WRITE,
			APPEND,
			UPDATE_READ,
			UPDATE_WRITE,
			UPDATE_APPEND
		}

		private static final Mode DEFAULT_MODE = Mode.READ;

		private static Mode mode(String modeString) {
			switch (modeString) {
				case "r": return Mode.READ;
				case "w": return Mode.WRITE;
				case "a": return Mode.APPEND;
				case "r+": return Mode.UPDATE_READ;
				case "w+": return Mode.UPDATE_WRITE;
				case "a+": return Mode.UPDATE_APPEND;
				default: return null;
			}
		}

		@Override
		protected String name() {
			return "open";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString filename = args.nextString();

			final boolean binary;
			final Mode mode;

			String modeString = args.hasNext() ? args.nextString().toString() : null;  // FIXME
			if (modeString != null) {
				if (modeString.endsWith("b")) {
					binary = true;
					modeString = modeString.substring(0, modeString.length() - 1);
				}
				else {
					binary = false;
				}

				mode = mode(modeString);
			}
			else {
				mode = DEFAULT_MODE;
				binary = false;
			}

			if (mode == null) {
				throw args.badArgument(1, "invalid mode");
			}

			IoFile file = null;
			try {
				file = lib.openFile(filename, mode);
			}
			catch (Exception ex) {
				context.getReturnBuffer().setTo(null, ex.getMessage());
				return;
			}

			assert (file != null);

			context.getReturnBuffer().setTo(file);
		}

	}

	static class Output extends AbstractLibFunction {

		private final IoLib lib;

		public Output(IoLib lib) {
			this.lib = Objects.requireNonNull(lib);
		}

		@Override
		protected String name() {
			return "output";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			if (args.hasNext()) {
				// open the argument for writing and set it as the default output file
				ByteString filename = args.nextString();
				IoFile f = lib.openFile(filename, Open.Mode.WRITE);
				assert (f != null);
				lib.setDefaultOutputFile(f);
				context.getReturnBuffer().setTo(f);
			}
			else {
				// return the default output file
				IoFile outFile = lib.getDefaultOutputFile();
				context.getReturnBuffer().setTo(outFile);
			}
		}

	}

	static class POpen extends UnimplementedFunction {
		// TODO
		public POpen(IoLib lib) {
			super("io.popen");
		}
	}

	static class Read extends AbstractLibFunction {

		private final IoLib lib;

		public Read(IoLib lib) {
			this.lib = Objects.requireNonNull(lib);
		}

		@Override
		protected String name() {
			return "read";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			IoFile file = lib.getDefaultInputFile();

			ArrayList<Object> callArgs = new ArrayList<>();
			callArgs.add(file);
			callArgs.addAll(Arrays.asList(args.getAll()));

			try {
				Dispatch.call(context, file_read(), callArgs.toArray());
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, null);
			}

			resume(context, file);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// results are already on the stack, this is a no-op
		}

	}

	static class Type extends AbstractLibFunction {

		@Override
		protected String name() {
			return "type";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object o = args.nextAny();

			final String result;

			if (o instanceof IoFile) {
				IoFile f = (IoFile) o;
				result = f.isClosed() ? "closed file" : "file";
			}
			else {
				result = null;
			}

			context.getReturnBuffer().setTo(result);
		}

	}

	static class TmpFile extends UnimplementedFunction {
		// TODO
		public TmpFile(IoLib lib) {
			super("io.tmpfile");
		}
	}

	static class Write extends AbstractLibFunction {

		private final IoLib lib;

		public Write(IoLib lib) {
			this.lib = Objects.requireNonNull(lib);
		}

		@Override
		protected String name() {
			return "write";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			IoFile file = lib.getDefaultOutputFile();

			ArrayList<Object> callArgs = new ArrayList<>();
			callArgs.add(file);
			callArgs.addAll(Arrays.asList(args.getAll()));

			try {
				Dispatch.call(context, file_write(), callArgs.toArray());
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, null);
			}

			resume(context, file);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// results are already on the stack, this is a no-op
		}

	}

}
