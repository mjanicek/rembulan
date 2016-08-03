package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.LuaRuntimeException;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.impl.DefaultUserdata;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

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

	public static class Close extends LibFunction {

		public static final Close INSTANCE = new Close();

		@Override
		protected String name() {
			return "close";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			final IOFile f = args.nextUserdata(typeName(), IOFile.class);

			FutureTask<Object> task = new FutureTask<>(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					f.close();
					return null;
				}
			});

			try {
				context.resumeAfter(task);
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, task);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			Future<?> future = (Future<?>) suspendedState;

			try {
				future.get();
			}
			catch (InterruptedException ex) {
				throw new LuaRuntimeException(ex);
			}
			// TODO: CancelledException ?
			catch (ExecutionException ex) {
				Throwable cause = ex.getCause();
				context.getObjectSink().setTo(null, cause != null ? cause.getMessage() : null);
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

			FutureTask<Object> task = new FutureTask<>(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					f.flush();
					return null;
				}
			});

			try {
				context.resumeAfter(task);
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, task);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			Future<?> future = (Future<?>) suspendedState;

			try {
				future.get();
			}
			catch (InterruptedException ex) {
				throw new LuaRuntimeException(ex);
			}
			// TODO: CancelledException ?
			catch (ExecutionException ex) {
				Throwable cause = ex.getCause();
				context.getObjectSink().setTo(null, cause != null ? cause.getMessage() : null);
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
			throw new UnsupportedOperationException();  // TODO
		}

	}

	public static class Seek extends LibFunction {

		public static final Seek INSTANCE = new Seek();

		@Override
		protected String name() {
			return "seek";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			throw new UnsupportedOperationException();  // TODO
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
				// TODO: CancelledException ?
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

