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

import net.sandius.rembulan.Function;
import net.sandius.rembulan.Metatables;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.TableFactory;
import net.sandius.rembulan.Userdata;
import net.sandius.rembulan.exec.ControlThrowable;
import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.BasicLib;
import net.sandius.rembulan.lib.IoLib;
import net.sandius.rembulan.lib.Lib;
import net.sandius.rembulan.lib.impl.io.InputStreamIoFile;
import net.sandius.rembulan.lib.impl.io.OutputStreamIoFile;
import net.sandius.rembulan.runtime.Dispatch;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.util.Check;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class DefaultIoLib extends IoLib {

	private final Function _close;
	private final Function _flush;
	private final Function _input;
	private final Function _lines;
	private final Function _open;
	private final Function _output;
	private final Function _popen;
	private final Function _read;
	private final Function _tmpfile;
	private final Function _write;

	private final Table fileMetatable;

	private final FileSystem fileSystem;

	private final IoFile stdIn;
	private final IoFile stdOut;
	private final IoFile stdErr;

	private IoFile defaultInput;
	private IoFile defaultOutput;

	public DefaultIoLib(
			TableFactory tableFactory,
			FileSystem fileSystem,
			InputStream in,
			OutputStream out,
			OutputStream err) {

		Check.notNull(tableFactory);
		Check.notNull(fileSystem);

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
		mt.rawset(BasicLib.MT_TOSTRING, _file_tostring());
		// TODO: set the __gc metamethod
		mt.rawset("close", _file_close());
		mt.rawset("flush", _file_flush());
		mt.rawset("lines", _file_lines());
		mt.rawset("read", _file_read());
		mt.rawset("seek", _file_seek());
		mt.rawset("setvbuf", _file_setvbuf());
		mt.rawset("write", _file_write());

		this.fileSystem = fileSystem;

		stdIn = in != null ? new InputStreamIoFile(in, fileMetatable, null) : null;
		stdOut = out != null ? new OutputStreamIoFile(out, fileMetatable, null) : null;
		stdErr = err != null ? new OutputStreamIoFile(err, fileMetatable, null) : null;

		defaultInput = stdIn;
		defaultOutput = stdOut;
	}

	public DefaultIoLib(TableFactory tableFactory) {
		this(tableFactory, FileSystems.getDefault(), System.in, System.out, System.err);
	}

	@Override
	public Function _close() {
		return _close;
	}

	@Override
	public Function _flush() {
		return _flush;
	}

	@Override
	public Function _input() {
		return _input;
	}

	@Override
	public Function _lines() {
		return _lines;
	}

	@Override
	public Function _open() {
		return _open;
	}

	@Override
	public Function _output() {
		return _output;
	}

	@Override
	public Function _popen() {
		return _popen;
	}

	@Override
	public Function _read() {
		return _read;
	}

	@Override
	public Function _tmpfile() {
		return _tmpfile;
	}

	@Override
	public Function _type() {
		return Type.INSTANCE;
	}

	@Override
	public Function _write() {
		return _write;
	}

	@Override
	public Userdata _stdin() {
		return stdIn;
	}

	@Override
	public Userdata _stdout() {
		return stdOut;
	}

	@Override
	public Userdata _stderr() {
		return stdErr;
	}

	@Override
	public Function _file_close() {
		return IoFile.Close.INSTANCE;
	}

	@Override
	public Function _file_flush() {
		return IoFile.Flush.INSTANCE;
	}

	@Override
	public Function _file_lines() {
		return IoFile.Lines.INSTANCE;
	}

	@Override
	public Function _file_read() {
		return IoFile.Read.INSTANCE;
	}

	@Override
	public Function _file_seek() {
		return IoFile.Seek.INSTANCE;
	}

	@Override
	public Function _file_setvbuf() {
		return IoFile.SetVBuf.INSTANCE;
	}

	@Override
	public Function _file_write() {
		return IoFile.Write.INSTANCE;
	}

	public Function _file_tostring() {
		return IoFile.ToString.INSTANCE;
	}

	private IoFile openFile(String filename, Open.Mode mode) {
		Check.notNull(filename);
		Check.notNull(mode);

		Path path = fileSystem.getPath(filename);

		throw new UnsupportedOperationException();  // TODO
	}

	public IoFile setDefaultInputFile(IoFile f) {
		defaultInput = Check.notNull(f);
		return f;
	}

	public IoFile getDefaultInputFile() {
		return defaultInput;
	}

	public IoFile setDefaultOutputFile(IoFile f) {
		defaultOutput = Check.notNull(f);
		return f;
	}

	public IoFile getDefaultOutputFile() {
		return defaultOutput;
	}

	public static class Close extends AbstractLibFunction {

		private final DefaultIoLib lib;

		public Close(DefaultIoLib lib) {
			this.lib = Check.notNull(lib);
		}

		@Override
		protected String name() {
			return "close";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			final IoFile file = args.hasNext()
					? args.nextUserdata(IoFile.typeName(), IoFile.class)
					: lib.getDefaultOutputFile();

			try {
				Dispatch.call(context, IoFile.Close.INSTANCE, file);
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			// results already on stack, this is a no-op
		}

	}

	public static class Flush extends AbstractLibFunction {

		private final DefaultIoLib lib;

		public Flush(DefaultIoLib lib) {
			this.lib = Check.notNull(lib);
		}

		@Override
		protected String name() {
			return "flush";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			IoFile outFile = lib.getDefaultOutputFile();

			try {
				Dispatch.call(context, IoFile.Flush.INSTANCE);
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, outFile);
			}

			resume(context, outFile);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			// results are already on the stack, this is a no-op
		}

	}

	public static class Input extends AbstractLibFunction {

		private final DefaultIoLib lib;

		public Input(DefaultIoLib lib) {
			this.lib = Check.notNull(lib);
		}

		@Override
		protected String name() {
			return "input";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			if (args.hasNext()) {
				// open the argument for reading and set it as the default input file
				String filename = args.nextString();
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

	public static class Open extends AbstractLibFunction {

		private final DefaultIoLib lib;

		public Open(DefaultIoLib lib) {
			this.lib = Check.notNull(lib);
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
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String filename = args.nextString();

			final boolean binary;
			final Mode mode;

			String modeString = args.hasNext() ? args.nextString() : null;
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

	public static class Output extends AbstractLibFunction {

		private final DefaultIoLib lib;

		public Output(DefaultIoLib lib) {
			this.lib = Check.notNull(lib);
		}

		@Override
		protected String name() {
			return "output";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			if (args.hasNext()) {
				// open the argument for writing and set it as the default output file
				String filename = args.nextString();
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

	public static class Read extends AbstractLibFunction {

		private final DefaultIoLib lib;

		public Read(DefaultIoLib lib) {
			this.lib = Check.notNull(lib);
		}

		@Override
		protected String name() {
			return "read";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			IoFile file = lib.getDefaultInputFile();

			ArrayList<Object> callArgs = new ArrayList<>();
			callArgs.add(file);
			callArgs.addAll(Arrays.asList(args.getAll()));

			try {
				Dispatch.call(context, IoFile.Read.INSTANCE, callArgs.toArray());
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, null);
			}

			resume(context, file);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			// results are already on the stack, this is a no-op
		}

	}

	public static class Type extends AbstractLibFunction {

		public static final Type INSTANCE = new Type();

		@Override
		protected String name() {
			return "type";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
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


	public static class Write extends AbstractLibFunction {

		private final DefaultIoLib lib;

		public Write(DefaultIoLib lib) {
			this.lib = Check.notNull(lib);
		}

		@Override
		protected String name() {
			return "write";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			IoFile file = lib.getDefaultOutputFile();

			ArrayList<Object> callArgs = new ArrayList<>();
			callArgs.add(file);
			callArgs.addAll(Arrays.asList(args.getAll()));

			try {
				Dispatch.call(context, IoFile.Write.INSTANCE, callArgs.toArray());
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, null);
			}

			resume(context, file);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			// results are already on the stack, this is a no-op
		}

	}

}
