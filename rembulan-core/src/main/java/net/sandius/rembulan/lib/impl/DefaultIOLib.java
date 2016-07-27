package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaRuntimeException;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;
import net.sandius.rembulan.core.Userdata;
import net.sandius.rembulan.core.impl.DefaultUserdata;
import net.sandius.rembulan.core.impl.Varargs;
import net.sandius.rembulan.lib.BasicLib;
import net.sandius.rembulan.lib.IOLib;
import net.sandius.rembulan.lib.LibUtils;
import net.sandius.rembulan.util.Check;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class DefaultIOLib extends IOLib {

	private final Table fileMetatable;

	private final FileSystem fileSystem;

	private IOFile defaultInput;
	private IOFile defaultOutput;

	public DefaultIOLib(TableFactory tableFactory, FileSystem fileSystem, InputStream in, OutputStream out) {
		Check.notNull(tableFactory);
		Check.notNull(fileSystem);

		// set up metatable for files
		Table mt = tableFactory.newTable();
		mt.rawset(Metatables.MT_INDEX, mt);
		mt.rawset(LibUtils.MT_NAME, IOFile.typeName());
		LibUtils.setIfNonNull(mt, BasicLib.MT_TOSTRING, _file_tostring());
		this.fileMetatable = mt;

		// TODO: set the __gc metamethod

		LibUtils.setIfNonNull(mt, "close", _file_close());
		LibUtils.setIfNonNull(mt, "flush", _file_flush());
		LibUtils.setIfNonNull(mt, "lines", _file_lines());
		LibUtils.setIfNonNull(mt, "read", _file_read());
		LibUtils.setIfNonNull(mt, "seek", _file_seek());
		LibUtils.setIfNonNull(mt, "setvbuf", _file_setvbuf());
		LibUtils.setIfNonNull(mt, "write", _file_write());

		this.fileSystem = fileSystem;

		defaultInput = in != null ? newFile(in, null) : null;
		defaultOutput = out != null ? newFile(null, out) : null;
	}

	public DefaultIOLib(TableFactory tableFactory) {
		this(tableFactory, FileSystems.getDefault(), System.in, System.out);
	}

	@Override
	public Function _close() {
		return null;  // TODO
	}

	@Override
	public Function _flush() {
		return null;  // TODO
	}

	@Override
	public Function _input() {
		return null;  // TODO
	}

	@Override
	public Function _lines() {
		return null;  // TODO
	}

	@Override
	public Function _open() {
		return null;  // TODO
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
		return null;  // TODO
	}

	@Override
	public Function _tmpfile() {
		return null;  // TODO
	}

	@Override
	public Function _type() {
		return null;  // TODO
	}

	@Override
	public Function _write() {
		return new Write(this);
	}

	@Override
	public Userdata _stdin() {
		return null;  // TODO
	}

	@Override
	public Userdata _stdout() {
		return null;  // TODO
	}

	@Override
	public Userdata _stderr() {
		return null;  // TODO
	}

	@Override
	public Function _file_close() {
		return null;  // TODO
	}

	@Override
	public Function _file_flush() {
		return null;  // TODO
	}

	@Override
	public Function _file_lines() {
		return null;  // TODO
	}

	@Override
	public Function _file_read() {
		return null;  // TODO
	}

	@Override
	public Function _file_seek() {
		return null;  // TODO
	}

	@Override
	public Function _file_setvbuf() {
		return null;  // TODO
	}

	@Override
	public Function _file_write() {
		return null;  // TODO
	}

	public Function _file_tostring() {
		return IOFile.ToString.INSTANCE;
	}

	protected IOFile newFile(InputStream in, OutputStream out) {
		return new IOFile(in, out, fileMetatable, null);
	}

	public static class IOFile extends DefaultUserdata {

		private final InputStream in;
		private final OutputStream out;

		public IOFile(InputStream in, OutputStream out, Table metatable, Object userValue) {
			super(metatable, userValue);
			this.in = in;
			this.out = out;
		}

		public static String typeName() {
			return "FILE*";
		}

		@Override
		public String toString() {
			return "file (0x" + Integer.toHexString(hashCode()) + ")";
		}

		public void write(String s) throws IOException {
			out.write(s.getBytes());
		}

		public static class ToString extends LibFunction {

			public static final ToString INSTANCE = new ToString();

			@Override
			protected String name() {
				return "tostring";
			}

			@Override
			protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
				IOFile f = args.nextUserdata(typeName(), IOFile.class);
				context.getObjectSink().setTo(f.toString());
			}

		}

		public static class Write extends LibFunction {

			public static final Write INSTANCE = new Write();

			@Override
			protected String name() {
				return "write";
			}

			private static class SavedState {

				public final IOFile file;
				public final ArgumentIterator args;
				public final Future<Object> currentTask;

				private SavedState(IOFile file, ArgumentIterator args, Future<Object> currentTask) {
					this.file = file;
					this.args = args;
					this.currentTask = currentTask;
				}

			}

			@Override
			protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
				final IOFile f = args.nextUserdata(typeName(), IOFile.class);
				run(context, f, args, null);
			}

			@Override
			public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
				SavedState ss = (SavedState) suspendedState;
				run(context, ss.file, ss.args, ss.currentTask);
			}

			private void run(ExecutionContext context, final IOFile file, ArgumentIterator args, Future<Object> currentTask) throws ControlThrowable {
				if (currentTask != null) {
					try {
						currentTask.get();
					}
					catch (InterruptedException ex) {
						throw new LuaRuntimeException(ex);
					}
					catch (ExecutionException ex) {
						Throwable cause = ex.getCause();
						context.getObjectSink().setTo(null, cause != null ? cause.getMessage() : null);
						return;
					}
				}

				if (args.hasNext()) {
					final String s = args.nextString();

					FutureTask<Object> task = new FutureTask<>(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							file.write(s);
							return null;
						}
					});

					try {
						context.resumeAfter(task);
					}
					catch (ControlThrowable ct) {
						throw ct.push(this, new SavedState(file, args, task));
					}
				}
				else {
					context.getObjectSink().setTo(file);
				}
			}

		}

	}

	public IOFile setDefaultOutputFile(IOFile f) {
		defaultOutput = Check.notNull(f);
		return f;
	}

	public IOFile getDefaultOutputFile() {
		return defaultOutput;
	}

	public static class Output extends LibFunction {

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
				throw new UnsupportedOperationException();  // TODO
			}
			else {
				// return the default output file
				IOFile outFile = lib.getDefaultOutputFile();
				context.getObjectSink().setTo(outFile);
			}
		}

	}

	public static class Write extends LibFunction {

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
			IOFile outFile = lib.getDefaultOutputFile();

			Object[] writeCallArgs = Varargs.concat(new Object[] { outFile }, args.getAll());

			try {
				Dispatch.call(context, IOFile.Write.INSTANCE, writeCallArgs);
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

}
