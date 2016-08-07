package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.impl.DefaultUserdata;

import java.io.IOException;

public abstract class IOFile extends DefaultUserdata {

	protected IOFile(Table metatable, Object userValue) {
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

	public abstract void write(String s) throws IOException;

	public enum Whence {
		BEGINNING,
		CURRENT_POSITION,
		END
	}

	public abstract long seek(Whence whence, long position) throws IOException;

	public static class Close extends LibFunction {

		public static final Close INSTANCE = new Close();

		@Override
		protected String name() {
			return "close";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			final IOFile f = args.nextUserdata(typeName(), IOFile.class);

			try {
				f.close();
			}
			catch (Exception ex) {
				context.getObjectSink().setTo(null, ex.getMessage());
				return;
			}

			context.getObjectSink().setTo(true);
		}

	}

	public static class Flush extends LibFunction {

		public static final Flush INSTANCE = new Flush();

		@Override
		protected String name() {
			return "flush";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			final IOFile f = args.nextUserdata(typeName(), IOFile.class);
			try {
				f.flush();
			}
			catch (Exception ex) {
				context.getObjectSink().setTo(null, ex.getMessage());
				return;
			}

			context.getObjectSink().setTo(true);
		}

	}

	public static class Lines extends LibFunction {

		public static final Lines INSTANCE = new Lines();

		@Override
		protected String name() {
			return "lines";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			throw new UnsupportedOperationException();  // TODO
		}

	}

	public static class Read extends LibFunction {

		public static final Read INSTANCE = new Read();

		@Override
		protected String name() {
			return "read";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			final IOFile f = args.nextUserdata(typeName(), IOFile.class);
			throw new UnsupportedOperationException();  // TODO
		}

	}

	public static class Seek extends LibFunction {

		public static final Seek INSTANCE = new Seek();

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
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			IOFile file = args.nextUserdata(typeName(), IOFile.class);

			final Whence whence;
			final long offset;

			if (args.hasNext()) {
				String s = args.nextString();
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
				context.getObjectSink().setTo(null, ex.getMessage());
				return;
			}

			context.getObjectSink().setTo(position);
		}

	}

	public static class SetVBuf extends LibFunction {

		public static final SetVBuf INSTANCE = new SetVBuf();

		@Override
		protected String name() {
			return "setvbuf";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			throw new UnsupportedOperationException();  // TODO
		}

	}

	public static class Write extends LibFunction {

		public static final Write INSTANCE = new Write();

		@Override
		protected String name() {
			return "write";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			final IOFile f = args.nextUserdata(typeName(), IOFile.class);
			while (args.hasNext()) {
				final String s = args.nextString();
				try {
					f.write(s);
				}
				catch (Exception ex) {
					context.getObjectSink().setTo(null, ex.getMessage());
					return;
				}
			}

			context.getObjectSink().setTo(f);
		}

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

}

