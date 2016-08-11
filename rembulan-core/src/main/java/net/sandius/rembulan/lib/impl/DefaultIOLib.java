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

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;
import net.sandius.rembulan.core.Userdata;
import net.sandius.rembulan.core.impl.Varargs;
import net.sandius.rembulan.lib.BasicLib;
import net.sandius.rembulan.lib.IOLib;
import net.sandius.rembulan.lib.Lib;
import net.sandius.rembulan.lib.impl.io.InputStreamIOFile;
import net.sandius.rembulan.lib.impl.io.OutputStreamIOFile;
import net.sandius.rembulan.util.Check;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class DefaultIOLib extends IOLib {

	private final Table fileMetatable;

	private final FileSystem fileSystem;

	private final IOFile stdIn;
	private final IOFile stdOut;
	private final IOFile stdErr;

	private IOFile defaultInput;
	private IOFile defaultOutput;

	public DefaultIOLib(
			TableFactory tableFactory,
			FileSystem fileSystem,
			InputStream in,
			OutputStream out,
			OutputStream err) {

		Check.notNull(tableFactory);
		Check.notNull(fileSystem);

		// set up metatable for files
		Table mt = tableFactory.newTable();
		this.fileMetatable = mt;

		mt.rawset(Metatables.MT_INDEX, mt);
		mt.rawset(Lib.MT_NAME, IOFile.typeName());
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

		stdIn = in != null ? new InputStreamIOFile(in, fileMetatable, null) : null;
		stdOut = out != null ? new OutputStreamIOFile(out, fileMetatable, null) : null;
		stdErr = err != null ? new OutputStreamIOFile(err, fileMetatable, null) : null;

		defaultInput = stdIn;
		defaultOutput = stdOut;
	}

	public DefaultIOLib(TableFactory tableFactory) {
		this(tableFactory, FileSystems.getDefault(), System.in, System.out, System.err);
	}

	@Override
	public Function _close() {
		return new Close(this);
	}

	@Override
	public Function _flush() {
		return new Flush(this);
	}

	@Override
	public Function _input() {
		return new Input(this);
	}

	@Override
	public Function _lines() {
		return null;  // TODO
	}

	@Override
	public Function _open() {
		return new Open(this);
	}

	@Override
	public Function _output() {
		return new Output(this);
	}

	@Override
	public Function _popen() {
		return null;  // TODO
	}

	@Override
	public Function _read() {
		return new Read(this);
	}

	@Override
	public Function _tmpfile() {
		return null;  // TODO
	}

	@Override
	public Function _type() {
		return Type.INSTANCE;
	}

	@Override
	public Function _write() {
		return new Write(this);
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
		return IOFile.Close.INSTANCE;
	}

	@Override
	public Function _file_flush() {
		return IOFile.Flush.INSTANCE;
	}

	@Override
	public Function _file_lines() {
		return IOFile.Lines.INSTANCE;
	}

	@Override
	public Function _file_read() {
		return IOFile.Read.INSTANCE;
	}

	@Override
	public Function _file_seek() {
		return IOFile.Seek.INSTANCE;
	}

	@Override
	public Function _file_setvbuf() {
		return IOFile.SetVBuf.INSTANCE;
	}

	@Override
	public Function _file_write() {
		return IOFile.Write.INSTANCE;
	}

	public Function _file_tostring() {
		return IOFile.ToString.INSTANCE;
	}

	private IOFile openFile(String filename, Open.Mode mode) {
		Check.notNull(filename);
		Check.notNull(mode);

		Path path = fileSystem.getPath(filename);

		throw new UnsupportedOperationException();  // TODO
	}

	public IOFile setDefaultInputFile(IOFile f) {
		defaultInput = Check.notNull(f);
		return f;
	}

	public IOFile getDefaultInputFile() {
		return defaultInput;
	}

	public IOFile setDefaultOutputFile(IOFile f) {
		defaultOutput = Check.notNull(f);
		return f;
	}

	public IOFile getDefaultOutputFile() {
		return defaultOutput;
	}

	public static class Close extends AbstractLibFunction {

		private final DefaultIOLib lib;

		public Close(DefaultIOLib lib) {
			this.lib = Check.notNull(lib);
		}

		@Override
		protected String name() {
			return "close";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			final IOFile file = args.hasNext()
					? args.nextUserdata(IOFile.typeName(), IOFile.class)
					: lib.getDefaultOutputFile();

			try {
				Dispatch.call(context, IOFile.Close.INSTANCE, file);
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

		private final DefaultIOLib lib;

		public Flush(DefaultIOLib lib) {
			this.lib = Check.notNull(lib);
		}

		@Override
		protected String name() {
			return "flush";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			IOFile outFile = lib.getDefaultOutputFile();

			try {
				Dispatch.call(context, IOFile.Flush.INSTANCE);
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

		private final DefaultIOLib lib;

		public Input(DefaultIOLib lib) {
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
				IOFile f = lib.openFile(filename, Open.Mode.READ);
				assert (f != null);
				lib.setDefaultInputFile(f);
				context.getObjectSink().setTo(f);
			}
			else {
				// return the default input file
				IOFile inFile = lib.getDefaultInputFile();
				context.getObjectSink().setTo(inFile);
			}
		}

	}

	public static class Open extends AbstractLibFunction {

		private final DefaultIOLib lib;

		public Open(DefaultIOLib lib) {
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

			IOFile file = null;
			try {
				file = lib.openFile(filename, mode);
			}
			catch (Exception ex) {
				context.getObjectSink().setTo(null, ex.getMessage());
				return;
			}

			assert (file != null);

			context.getObjectSink().setTo(file);
		}

	}

	public static class Output extends AbstractLibFunction {

		private final DefaultIOLib lib;

		public Output(DefaultIOLib lib) {
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
				IOFile f = lib.openFile(filename, Open.Mode.WRITE);
				assert (f != null);
				lib.setDefaultOutputFile(f);
				context.getObjectSink().setTo(f);
			}
			else {
				// return the default output file
				IOFile outFile = lib.getDefaultOutputFile();
				context.getObjectSink().setTo(outFile);
			}
		}

	}

	public static class Read extends AbstractLibFunction {

		private final DefaultIOLib lib;

		public Read(DefaultIOLib lib) {
			this.lib = Check.notNull(lib);
		}

		@Override
		protected String name() {
			return "read";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			IOFile file = lib.getDefaultInputFile();
			Object[] callArgs = Varargs.concat(new Object[] { file }, args.getAll());

			try {
				Dispatch.call(context, IOFile.Read.INSTANCE, callArgs);
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

			if (o instanceof IOFile) {
				IOFile f = (IOFile) o;
				result = f.isClosed() ? "closed file" : "file";
			}
			else {
				result = null;
			}

			context.getObjectSink().setTo(result);
		}

	}


	public static class Write extends AbstractLibFunction {

		private final DefaultIOLib lib;

		public Write(DefaultIOLib lib) {
			this.lib = Check.notNull(lib);
		}

		@Override
		protected String name() {
			return "write";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			IOFile file = lib.getDefaultOutputFile();
			Object[] callArgs = Varargs.concat(new Object[] { file }, args.getAll());

			try {
				Dispatch.call(context, IOFile.Write.INSTANCE, callArgs);
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
