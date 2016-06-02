package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.Preemption;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;
import net.sandius.rembulan.core.Userdata;
import net.sandius.rembulan.core.impl.DefaultUserdata;
import net.sandius.rembulan.core.impl.Varargs;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.BasicLib;
import net.sandius.rembulan.lib.IOLib;
import net.sandius.rembulan.lib.LibUtils;
import net.sandius.rembulan.util.Check;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

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
		return new IOFile(in, out, fileMetatable);
	}

	public static class IOFile extends DefaultUserdata {

		private final InputStream in;
		private final OutputStream out;

		public IOFile(InputStream in, OutputStream out, Table metatable) {
			super(metatable);
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

		protected static IOFile nextFile(LibFunction.CallArguments args) {
			Check.notNull(args);

			if (args.hasNext()) {
				Object o = args.optNextAny();
				if (o instanceof IOFile) {
					return (IOFile) o;
				}
				else {
					throw new BadArgumentException(1, args.name, typeName() + " expected, got " + args.namer().typeNameOf(o));
				}
			}
			else {
				throw new BadArgumentException(1, args.name, typeName() + " expected, got no value");
			}
		}

		public static class ToString extends LibFunction {

			public static final ToString INSTANCE = new ToString();

			@Override
			protected String name() {
				return "tostring";
			}

			@Override
			protected Preemption invoke(ExecutionContext context, CallArguments args) {
				IOFile f = nextFile(args);
				context.getObjectSink().setTo(f.toString());
				return null;
			}

		}

		public static class Write extends LibFunction {

			public static final Write INSTANCE = new Write();

			@Override
			protected String name() {
				return "write";
			}

			@Override
			protected Preemption invoke(ExecutionContext context, CallArguments args) {
				IOFile f = nextFile(args);

				while (args.hasNext()) {
					String s = args.nextString();
					try {
						f.write(s);
						context.getObjectSink().setTo(f);
					}
					catch (IOException ex) {
						context.getObjectSink().setTo(null, ex.getMessage());
						throw new RuntimeException(ex);
					}
				}

				return null;
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
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			if (args.hasNext()) {
				// open the argument for writing and set it as the default output file
				throw new UnsupportedOperationException();  // TODO
			}
			else {
				// return the default output file
				IOFile outFile = lib.getDefaultOutputFile();
				context.getObjectSink().setTo(outFile);
				return null;
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
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			IOFile outFile = lib.getDefaultOutputFile();

			Object[] writeCallArgs = Varargs.concat(new Object[] { outFile }, args.getAll());

			try {
				Dispatch.call(context, IOFile.Write.INSTANCE, writeCallArgs);
			}
			catch (ControlThrowable ct) {
				ct.push(this, outFile);
				return ct.toPreemption();
			}

			return _resume(context, outFile);
		}

		@Override
		protected Preemption _resume(ExecutionContext context, Object suspendedState) {
			// results are already on the stack, this is a no-op
			return null;
		}

	}

}
