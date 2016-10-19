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

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Metatables;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.TableFactory;
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.Lib;
import net.sandius.rembulan.lib.ModuleLibHelper;
import net.sandius.rembulan.lib.impl.io.InputStreamIoFile;
import net.sandius.rembulan.lib.impl.io.OutputStreamIoFile;
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
public final class DefaultIoLib {

	/**
	 * {@code io.type (obj)}
	 *
	 * <p>Checks whether {@code obj} is a valid file handle. Returns the string {@code "file"}
	 * if {@code obj} is an open file handle, {@code "closed file"} if {@code obj} is
	 * a closed file handle, or <b>nil</b> if {@code obj} is not a file handle.</p>
	 */
	public static final LuaFunction TYPE = new Type();

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

	private DefaultIoLib(
			TableFactory tableFactory,
			FileSystem fileSystem,
			InputStream in,
			OutputStream out,
			OutputStream err) {

		Objects.requireNonNull(tableFactory);
		Objects.requireNonNull(fileSystem);

		this._close = new Close(this);
		this._flush = new Flush(this);
		this._input = new Input(this);
		this._lines = new UnimplementedFunction("io.lines");  // TODO
		this._open = new Open(this);
		this._output = new Output(this);
		this._popen = new UnimplementedFunction("io.popen");  // TODO
		this._read = new Read(this);
		this._tmpfile = new UnimplementedFunction("io.tmpfile");  // TODO
		this._write = new Write(this);

		// set up metatable for files
		Table mt = tableFactory.newTable();
		this.fileMetatable = mt;

		mt.rawset(Metatables.MT_INDEX, mt);
		mt.rawset(Lib.MT_NAME, IoFile.typeName());
		mt.rawset(DefaultBasicLib.MT_TOSTRING, IoFile.TOSTRING);
		// TODO: set the __gc metamethod
		mt.rawset("close", IoFile.CLOSE);
		mt.rawset("flush", IoFile.FLUSH);
		mt.rawset("lines", IoFile.LINES);
		mt.rawset("read", IoFile.READ);
		mt.rawset("seek", IoFile.SEEK);
		mt.rawset("setvbuf", IoFile.SETVBUF);
		mt.rawset("write", IoFile.WRITE);

		this.fileSystem = fileSystem;

		stdIn = in != null ? new InputStreamIoFile(in, fileMetatable, null) : null;
		stdOut = out != null ? new OutputStreamIoFile(out, fileMetatable, null) : null;
		stdErr = err != null ? new OutputStreamIoFile(err, fileMetatable, null) : null;

		defaultInput = stdIn;
		defaultOutput = stdOut;
	}

	public static void installInto(StateContext context, Table env, RuntimeEnvironment runtimeEnvironment) {
		Table t = context.newTable();

		// FIXME
		DefaultIoLib l = new DefaultIoLib(
				context,
				runtimeEnvironment.fileSystem(),
				runtimeEnvironment.standardInput(),
				runtimeEnvironment.standardOutput(),
				runtimeEnvironment.standardError());

		t.rawset("close", l._close);
		t.rawset("flush", l._flush);
		t.rawset("input", l._input);
		t.rawset("lines", l._lines);
		t.rawset("open", l._open);
		t.rawset("output", l._output);
		t.rawset("popen", l._popen);
		t.rawset("read", l._read);
		t.rawset("tmpfile", l._tmpfile);
		t.rawset("type", TYPE);
		t.rawset("write", l._write);

		t.rawset("stdin", l.stdIn);
		t.rawset("stdout", l.stdOut);
		t.rawset("stderr", l.stdErr);

		ModuleLibHelper.install(env, "io", t);
	}


	private IoFile openFile(ByteString filename, Open.Mode mode) {
		Objects.requireNonNull(filename);
		Objects.requireNonNull(mode);

		Path path = fileSystem.getPath(filename.toString());

		throw new UnsupportedOperationException();  // TODO
	}

	public IoFile setDefaultInputFile(IoFile f) {
		defaultInput = Objects.requireNonNull(f);
		return f;
	}

	public IoFile getDefaultInputFile() {
		return defaultInput;
	}

	public IoFile setDefaultOutputFile(IoFile f) {
		defaultOutput = Objects.requireNonNull(f);
		return f;
	}

	public IoFile getDefaultOutputFile() {
		return defaultOutput;
	}

	static class Close extends AbstractLibFunction {

		private final DefaultIoLib lib;

		public Close(DefaultIoLib lib) {
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
				Dispatch.call(context, IoFile.CLOSE, file);
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

		private final DefaultIoLib lib;

		public Flush(DefaultIoLib lib) {
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
				Dispatch.call(context, IoFile.FLUSH);
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

		private final DefaultIoLib lib;

		public Input(DefaultIoLib lib) {
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

	static class Open extends AbstractLibFunction {

		private final DefaultIoLib lib;

		public Open(DefaultIoLib lib) {
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

		private final DefaultIoLib lib;

		public Output(DefaultIoLib lib) {
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

	static class Read extends AbstractLibFunction {

		private final DefaultIoLib lib;

		public Read(DefaultIoLib lib) {
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
				Dispatch.call(context, IoFile.READ, callArgs.toArray());
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


	static class Write extends AbstractLibFunction {

		private final DefaultIoLib lib;

		public Write(DefaultIoLib lib) {
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
				Dispatch.call(context, IoFile.WRITE, callArgs.toArray());
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
