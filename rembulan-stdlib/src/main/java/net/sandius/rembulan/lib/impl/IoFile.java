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

import net.sandius.rembulan.Table;
import net.sandius.rembulan.exec.ControlThrowable;
import net.sandius.rembulan.impl.DefaultUserdata;
import net.sandius.rembulan.runtime.ExecutionContext;

import java.io.IOException;

public abstract class IoFile extends DefaultUserdata {

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

	public abstract void write(String s) throws IOException;

	public enum Whence {
		BEGINNING,
		CURRENT_POSITION,
		END
	}

	public abstract long seek(Whence whence, long position) throws IOException;

	public static class Close extends AbstractLibFunction {

		public static final Close INSTANCE = new Close();

		@Override
		protected String name() {
			return "close";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
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

	public static class Flush extends AbstractLibFunction {

		public static final Flush INSTANCE = new Flush();

		@Override
		protected String name() {
			return "flush";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
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

	public static class Lines extends AbstractLibFunction {

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

	public static class Read extends AbstractLibFunction {

		public static final Read INSTANCE = new Read();

		@Override
		protected String name() {
			return "read";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			final IoFile f = args.nextUserdata(typeName(), IoFile.class);
			throw new UnsupportedOperationException();  // TODO
		}

	}

	public static class Seek extends AbstractLibFunction {

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
			IoFile file = args.nextUserdata(typeName(), IoFile.class);

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
				context.getReturnBuffer().setTo(null, ex.getMessage());
				return;
			}

			context.getReturnBuffer().setTo(position);
		}

	}

	public static class SetVBuf extends AbstractLibFunction {

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

	public static class Write extends AbstractLibFunction {

		public static final Write INSTANCE = new Write();

		@Override
		protected String name() {
			return "write";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			final IoFile f = args.nextUserdata(typeName(), IoFile.class);
			while (args.hasNext()) {
				final String s = args.nextString();
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

	public static class ToString extends AbstractLibFunction {

		public static final ToString INSTANCE = new ToString();

		@Override
		protected String name() {
			return "tostring";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			IoFile f = args.nextUserdata(typeName(), IoFile.class);
			context.getReturnBuffer().setTo(f.toString());
		}

	}

}

