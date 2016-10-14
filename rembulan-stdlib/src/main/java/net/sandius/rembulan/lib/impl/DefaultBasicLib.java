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

import net.sandius.rembulan.*;
import net.sandius.rembulan.lib.AssertionFailedException;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.BasicLib;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.runtime.Dispatch;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.IllegalOperationAttemptException;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ProtectedResumable;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.ReturnBuffer;
import net.sandius.rembulan.runtime.UnresolvedControlThrowable;
import net.sandius.rembulan.util.Check;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class DefaultBasicLib extends BasicLib {

	private final LuaFunction _print;
	private final LuaFunction _dofile;
	private final LuaFunction _load;
	private final LuaFunction _loadfile;

	public DefaultBasicLib(PrintStream out, ChunkLoader loader, Object defaultEnv, FileSystem fileSystem) {
		this._print = out != null ? new Print(out) : null;

		if (loader != null) {
			this._load = new Load(loader, defaultEnv);
		}
		else {
			// no loader supplied
			this._load = null;
		}

		if (loader != null && fileSystem != null) {
			this._loadfile = new LoadFile(fileSystem, loader, defaultEnv);
			this._dofile = new DoFile(fileSystem, loader, defaultEnv);
		}
		else {
			this._loadfile = null;
			this._dofile = null;
		}

	}

	@Deprecated
	public DefaultBasicLib(PrintStream out, ChunkLoader loader, Object defaultEnv) {
		this(out, loader, defaultEnv, null);
	}

	@Override
	public String __VERSION() {
		return "Lua 5.3";
	}

	@Override
	public LuaFunction _print() {
		return _print;
	}

	@Override
	public LuaFunction _type() {
		return Type.INSTANCE;
	}

	@Override
	public LuaFunction _next() {
		return Next.INSTANCE;
	}

	@Override
	public LuaFunction _pairs() {
		return Pairs.INSTANCE;
	}

	@Override
	public LuaFunction _ipairs() {
		return IPairs.INSTANCE;
	}

	@Override
	public LuaFunction _tostring() {
		return ToString.INSTANCE;
	}

	@Override
	public LuaFunction _tonumber() {
		return ToNumber.INSTANCE;
	}

	@Override
	public LuaFunction _error() {
		return Error.INSTANCE;
	}

	@Override
	public LuaFunction _assert() {
		return Assert.INSTANCE;
	}

	@Override
	public LuaFunction _getmetatable() {
		return GetMetatable.INSTANCE;
	}

	@Override
	public LuaFunction _setmetatable() {
		return SetMetatable.INSTANCE;
	}

	@Override
	public LuaFunction _pcall() {
		return PCall.INSTANCE;
	}

	@Override
	public LuaFunction _xpcall() {
		return XPCall.INSTANCE;
	}

	@Override
	public LuaFunction _rawequal() {
		return RawEqual.INSTANCE;
	}

	@Override
	public LuaFunction _rawget() {
		return RawGet.INSTANCE;
	}

	@Override
	public LuaFunction _rawset() {
		return RawSet.INSTANCE;
	}

	@Override
	public LuaFunction _rawlen() {
		return RawLen.INSTANCE;
	}

	@Override
	public LuaFunction _select() {
		return Select.INSTANCE;
	}

	@Override
	public LuaFunction _collectgarbage() {
		return CollectGarbage.INSTANCE;
	}

	@Override
	public LuaFunction _dofile() {
		return _dofile;
	}

	@Override
	public LuaFunction _load() {
		return _load;
	}

	@Override
	public LuaFunction _loadfile() {
		return _loadfile;
	}


	public static class Print extends AbstractLibFunction {

		private final PrintStream out;

		public Print(PrintStream out) {
			this.out = Check.notNull(out);
		}

		@Override
		protected String name() {
			return "print";
		}

		private void run(ExecutionContext context, Object[] args) throws ResolvedControlThrowable {
			for (int i = 0; i < args.length; i++) {
				Object a = args[i];
				try {
					Dispatch.call(context, ToString.INSTANCE, a);
				}
				catch (UnresolvedControlThrowable ct) {
					throw ct.resolve(this, Arrays.copyOfRange(args, i + 1, args.length));
				}

				Object s = context.getReturnBuffer().get0();
				if (LuaType.isString(s)) {
					out.print(s);
				}
				else {
					throw new LuaRuntimeException("error calling 'print' ('tostring' must return a string to 'print')");
				}

				if (i + 1 < args.length) {
					out.print('\t');
				}
			}
			out.println();

			// returning nothing
			context.getReturnBuffer().setTo();
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			run(context, args.getAll());
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			run(context, (Object[]) suspendedState);
		}

	}

	public static class Type extends AbstractLibFunction {

		public static final Type INSTANCE = new Type();

		@Override
		protected String name() {
			return "type";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			ByteString typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(args.nextAny());
			context.getReturnBuffer().setTo(typeName);
		}

	}

	public static class Next extends AbstractLibFunction {

		public static final Next INSTANCE = new Next();

		@Override
		protected String name() {
			return "next";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table table = args.nextTable();
			Object index = args.optNextAny();

			final Object nxt;

			if (index != null) {
				nxt = table.successorKeyOf(index);
			}
			else {
				nxt = table.initialKey();
			}

			if (nxt == null) {
				// we've reached the end
				context.getReturnBuffer().setTo(null);
			}
			else {
				Object value = table.rawget(nxt);
				context.getReturnBuffer().setTo(nxt, value);
			}
		}

	}

	public static class INext extends AbstractLibFunction {

		public static final INext INSTANCE = new INext();

		@Override
		protected String name() {
			return "inext";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args)
				throws ResolvedControlThrowable {

			Table table = args.nextTable();
			long index = args.nextInteger();

			index += 1;

			try {
				Dispatch.index(context, table, index);
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, index);
			}

			Object result = context.getReturnBuffer().get0();
			processResult(context, index, result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			long index = (Long) suspendedState;
			Object result = context.getReturnBuffer().get0();
			processResult(context, index, result);
		}

		private static void processResult(ExecutionContext context, long index, Object o) throws ResolvedControlThrowable {
			if (o != null) {
				context.getReturnBuffer().setTo(index, o);
			}
			else {
				context.getReturnBuffer().setTo(null);
			}
		}

	}

	public static class Pairs extends AbstractLibFunction {

		public static final Pairs INSTANCE = new Pairs();

		@Override
		protected String name() {
			return "pairs";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table t = args.nextTable();
			Object metamethod = Metatables.getMetamethod(context, MT_PAIRS, t);

			if (metamethod != null) {
				try {
					Dispatch.call(context, metamethod, t);
				}
				catch (UnresolvedControlThrowable ct) {
					throw ct.resolve(this, null);
				}

				ReturnBuffer rbuf = context.getReturnBuffer();
				rbuf.setTo(rbuf.get0(), rbuf.get1(), rbuf.get2());
			}
			else {
				ReturnBuffer rbuf = context.getReturnBuffer();
				rbuf.setTo(Next.INSTANCE, t, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			ReturnBuffer rbuf = context.getReturnBuffer();
			rbuf.setTo(rbuf.get0(), rbuf.get1(), rbuf.get2());
		}

	}

	public static class IPairs extends AbstractLibFunction {

		public static final IPairs INSTANCE = new IPairs();

		@Override
		protected String name() {
			return "ipairs";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table t = args.nextTable();
			context.getReturnBuffer().setTo(INext.INSTANCE, t, 0L);
		}

	}

	public static class ToString extends AbstractLibFunction {

		public static final ToString INSTANCE = new ToString();

		@Override
		protected String name() {
			return "tostring";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object arg = args.nextAny();

			Object meta = Metatables.getMetamethod(context, MT_TOSTRING, arg);
			if (meta != null) {
				try {
					Dispatch.call(context, meta, arg);
				}
				catch (UnresolvedControlThrowable ct) {
					throw ct.resolve(this, null);
				}

				// resume
				resume(context, null);
			}
			else {
				// no metamethod, just call the default toString
				ByteString s = Conversions.toHumanReadableString(arg);
				context.getReturnBuffer().setTo(s);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// trim to single value
			Object result = context.getReturnBuffer().get0();
			context.getReturnBuffer().setTo(result);
		}

	}

	public static class ToNumber extends AbstractLibFunction {

		public static final ToNumber INSTANCE = new ToNumber();

		public static Long toNumber(ByteString s, int base) {
			try {
				return Long.parseLong(s.toString().trim(), base);
			}
			catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		protected String name() {
			return "tonumber";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			if (args.size() < 2) {
				// no base
				Object o = args.nextAny();
				Number n = Conversions.numericalValueOf(o);
				context.getReturnBuffer().setTo(n);
			}
			else {
				// We do the argument checking gymnastics in order to achieve the same error
				// reporting as in PUC-Lua. We first check that base (#2) is an integer, then
				// retrieve the string (#1), and then check that the base is within range.

				args.skip();
				args.nextInteger();
				args.rewind();
				ByteString s = args.nextStrictString();
				int base = args.nextIntRange("base", Character.MIN_RADIX, Character.MAX_RADIX);

				context.getReturnBuffer().setTo(toNumber(s, base));

			}
		}

	}


	public static class GetMetatable extends AbstractLibFunction {

		public static final GetMetatable INSTANCE = new GetMetatable();

		@Override
		protected String name() {
			return "getmetatable";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object arg = args.nextAny();
			Object meta = Metatables.getMetamethod(context, MT_METATABLE, arg);

			Object result = meta != null
					? meta  // __metatable field present, return its value
					: context.getMetatable(arg);  // return the entire metatable

			context.getReturnBuffer().setTo(result);
		}

	}

	public static class SetMetatable extends AbstractLibFunction {

		public static final SetMetatable INSTANCE = new SetMetatable();

		@Override
		protected String name() {
			return "setmetatable";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table t = args.nextTable();
			Table mt = args.nextTableOrNil();

			if (Metatables.getMetamethod(context, MT_METATABLE, t) != null) {
				throw new IllegalOperationAttemptException("cannot change a protected metatable");
			}
			else {
				t.setMetatable(mt);
				context.getReturnBuffer().setTo(t);
			}
		}

	}

	public static class Error extends AbstractLibFunction {

		public static final Error INSTANCE = new Error();

		@Override
		protected String name() {
			return "error";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			// TODO: handle levels
			Object arg1 = args.optNextAny();
			throw new LuaRuntimeException(arg1);
		}

	}

	public static class Assert extends AbstractLibFunction {

		public static final Assert INSTANCE = new Assert();

		@Override
		protected String name() {
			return "assert";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			if (Conversions.booleanValueOf(args.nextAny())) {
				context.getReturnBuffer().setToContentsOf(args.getAll());
			}
			else {
				final AssertionFailedException ex;
				if (args.hasNext()) {
					// message is defined
					Object message = args.nextAny();
					ByteString stringMessage = Conversions.stringValueOf(message);
					if (stringMessage != null) {
						ex = new AssertionFailedException(stringMessage);
					}
					else {
						ex = new AssertionFailedException(message);
					}
				}
				else {
					// message not defined, use the default
					ex = new AssertionFailedException("assertion failed!");
				}

				throw ex;
			}

		}

	}

	public static class PCall extends AbstractLibFunction implements ProtectedResumable {

		public static final PCall INSTANCE = new PCall();

		@Override
		protected String name() {
			return "pcall";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object callTarget = args.nextAny();
			Object[] callArgs = args.getTail();

			try {
				Dispatch.call(context, callTarget, callArgs);
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, null);
			}
			catch (Exception ex) {
				resumeError(context, null, Conversions.toErrorObject(ex));
				return;
			}

			resume(context, null);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// success: prepend true
			ReturnBuffer rbuf = context.getReturnBuffer();
			ArrayList<Object> result = new ArrayList<>();
			result.add(Boolean.TRUE);
			result.addAll(Arrays.asList(rbuf.getAsArray()));
			rbuf.setToContentsOf(result);
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ResolvedControlThrowable {
			context.getReturnBuffer().setTo(Boolean.FALSE, error);  // failure
		}

	}

	public static class XPCall extends AbstractLibFunction implements ProtectedResumable {

		public static final int MAX_DEPTH = 220;  // 220 in PUC-Lua 5.3

		public static final XPCall INSTANCE = new XPCall();

		@Override
		protected String name() {
			return "xpcall";
		}

		private static class SavedState {
			public final LuaFunction handler;
			public final int depth;

			private SavedState(LuaFunction handler, int depth) {
				this.handler = handler;
				this.depth = depth;
			}
		}

		private static void prependTrue(ExecutionContext context) {
			ReturnBuffer rbuf = context.getReturnBuffer();
			ArrayList<Object> result = new ArrayList<>();
			result.add(Boolean.TRUE);
			result.addAll(Arrays.asList(rbuf.getAsArray()));
			rbuf.setToContentsOf(result);
		}

		private static void prependFalseAndTrim(ExecutionContext context) {
			ReturnBuffer rbuf = context.getReturnBuffer();
			Object errorObject = rbuf.get0();
			rbuf.setTo(Boolean.FALSE, errorObject);
		}

		private void handleError(ExecutionContext context, LuaFunction handler, int depth, Object errorObject) throws ResolvedControlThrowable {
			// we want to be able to handle nil error objects, so we need a separate flag
			boolean isError = true;

			while (isError && depth < MAX_DEPTH) {
				depth += 1;

				try {
					Dispatch.call(context, handler, errorObject);
					isError = false;
				}
				catch (UnresolvedControlThrowable ct) {
					throw ct.resolve(this, new SavedState(handler, depth));
				}
				catch (Exception e) {
					errorObject = Conversions.toErrorObject(e);
					isError = true;
				}
			}

			if (!isError) {
				prependFalseAndTrim(context);
			}
			else {
				// depth must be >= MAX_DEPTH
				context.getReturnBuffer().setTo(Boolean.FALSE, "error in error handling");
			}
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object callTarget = args.peekOrNil();
			args.skip();
			LuaFunction handler = args.nextFunction();
			Object[] callArgs = args.getTail();

			Object errorObject = null;
			boolean isError = false;  // need to distinguish nil error objects from no-error

			try {
				Dispatch.call(context, callTarget, callArgs);
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, new SavedState(handler, 0));
			}
			catch (Exception e) {
				errorObject = Conversions.toErrorObject(e);
				isError = true;
			}

			if (!isError) {
				prependTrue(context);
			}
			else {
				handleError(context, handler, 0, errorObject);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			SavedState ss = (SavedState) suspendedState;
			if (ss.depth == 0) {
				prependTrue(context);
			}
			else {
				prependFalseAndTrim(context);
			}
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ResolvedControlThrowable {
			SavedState ss = (SavedState) suspendedState;
			handleError(context, ss.handler, ss.depth, error);
		}

	}

	public static class RawEqual extends AbstractLibFunction {

		public static final RawEqual INSTANCE = new RawEqual();

		@Override
		protected String name() {
			return "rawequal";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object a = args.nextAny();
			Object b = args.nextAny();
			context.getReturnBuffer().setTo(Ordering.isRawEqual(a, b));
		}

	}

	public static class RawGet extends AbstractLibFunction {

		public static final RawGet INSTANCE = new RawGet();

		@Override
		protected String name() {
			return "rawget";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table table = args.nextTable();
			Object key = args.nextAny();
			context.getReturnBuffer().setTo(table.rawget(key));
		}

	}

	public static class RawSet extends AbstractLibFunction {

		public static final RawSet INSTANCE = new RawSet();

		@Override
		protected String name() {
			return "rawset";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table table = args.nextTable();
			Object key = args.nextAny();
			Object value = args.nextAny();

			table.rawset(key, value);
			context.getReturnBuffer().setTo(table);
		}

	}

	public static class RawLen extends AbstractLibFunction {

		public static final RawLen INSTANCE = new RawLen();

		@Override
		protected String name() {
			return "rawlen";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			final long result;

			// no need to distinguish missing value vs nil
			Object arg1 = args.optNextAny();

			if (arg1 instanceof Table) {
				Table table = (Table) arg1;
				result = table.rawlen();
			}
			else if (arg1 instanceof ByteString) {
				ByteString s = (ByteString) arg1;
				result = (long) s.length();
			}
			else if (arg1 instanceof String) {
				String s = (String) arg1;
				result = Dispatch.len(s);
			}
			else {
				throw new BadArgumentException(1, name(), "table or string expected");
			}

			context.getReturnBuffer().setTo(result);
		}

	}

	public static class Select extends AbstractLibFunction {

		public static final Select INSTANCE = new Select();

		@Override
		protected String name() {
			return "select";
		}

		private static boolean isHash(Object o) {
			if (o instanceof ByteString) return ((ByteString) o).startsWith((byte) '#');
			else if (o instanceof String) return ((String) o).startsWith("#");
			else return false;
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object index = args.peekOrNil();

			if (isHash(index)) {
				// return the number of remaining args
				int count = args.size() - 1;
				context.getReturnBuffer().setTo((long) count);
			}
			else {
				int idx = args.nextIntRange("index", -args.size() + 1, Integer.MAX_VALUE);

				int from = idx >= 0
						? idx  // from the beginning
						: args.size() + idx;  // idx < 0: from the end (-1 is the last index)

				if (from < 1) {
					throw new BadArgumentException(1, name(), "index out of range");
				}

				Object[] r = args.getAll();
				final Object[] result;
				result = from > r.length ? new Object[0] : Arrays.copyOfRange(r, from, r.length);
				context.getReturnBuffer().setToContentsOf(result);
			}
		}

	}

	public static class CollectGarbage extends AbstractLibFunction {

		public static final CollectGarbage INSTANCE = new CollectGarbage();

		@Override
		protected String name() {
			return "collectgarbage";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			if (args.hasNext()) {
				throw new UnsupportedOperationException();  // TODO
			}
			// TODO
		}

	}

	public static class Load extends AbstractLibFunction {

		static final ByteString DEFAULT_MODE = ByteString.constOf("bt");

		private final ChunkLoader loader;
		private final Object defaultEnv;

		public Load(ChunkLoader loader, Object env) {
			this.loader = Check.notNull(loader);
			this.defaultEnv = env;
		}

		@Override
		protected String name() {
			return "load";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {

			// chunk
			final Object chunk = args.hasNext() && Conversions.stringValueOf(args.peek()) != null
					? args.nextString()
					: args.nextFunction();

			assert (chunk != null);

			// chunk name
			final String chunkName;
			if (args.hasNext() && args.peek() != null) {
				chunkName = "[string \"" + args.nextString() + "\"]";
			}
			else {
				if (args.hasNext()) args.skip();  // next is nil
				chunkName = chunk instanceof ByteString
						? "[string \"" + chunk + "\"]"
						: "=(load)";
			}

			// mode
			final ByteString modeString = args.hasNext()
					? args.nextString()
					: DEFAULT_MODE;

			final Object env = args.hasNext()
					? args.nextAny()
					: defaultEnv;

			// TODO: binary chunks

			if (!modeString.contains((byte) 't')) {
				ByteStringBuilder bld = new ByteStringBuilder();
				bld.append("attempt to load a text chunk (mode is '").append(modeString).append("')");
				context.getReturnBuffer().setTo(null, bld.toByteString());
			}
			else {
				if (chunk instanceof ByteString) {
					loadFromString(context, chunkName, env, (ByteString) chunk);
				}
				else {
					LuaFunction fn = (LuaFunction) chunk;
					loadFromFunction(context, false, chunkName, env, new ByteStringBuilder(), fn);
				}
			}

		}

		private void loadFromString(ExecutionContext context, String chunkName, Object env, ByteString chunkText) {
			final LuaFunction fn;
			try {
				fn = loader.loadTextChunk(new Variable(env), chunkName, chunkText.toString());
			}
			catch (LoaderException ex) {
				context.getReturnBuffer().setTo(null, ex.getLuaStyleErrorMessage());
				return;
			}

			if (fn != null) {
				context.getReturnBuffer().setTo(fn);
			}
			else {
				// don't trust the loader to return a non-null value
				context.getReturnBuffer().setTo(null, "loader returned nil");
			}
		}

		private static class State {

			public final String chunkName;
			public final Object env;
			public final ByteStringBuilder bld;
			public final LuaFunction fn;

			private State(String chunkName, Object env, ByteStringBuilder bld, LuaFunction fn) {
				this.chunkName = chunkName;
				this.env = env;
				this.bld = bld;
				this.fn = fn;
			}

		}

		private void loadFromFunction(ExecutionContext context, boolean resuming, String chunkName, Object env, ByteStringBuilder bld, LuaFunction fn)
				throws ResolvedControlThrowable {

			ByteString chunkText = null;

			try {
				while (chunkText == null) {
					if (!resuming) {
						Dispatch.call(context, fn);
					}

					resuming = false;

					Object o = context.getReturnBuffer().get0();
					if (o == null) {
						chunkText = bld.toByteString();
					}
					else {
						ByteString s = Conversions.stringValueOf(o);
						if (s != null) {
							bld.append(s);
						}
						else {
							context.getReturnBuffer().setTo(null, "reader function must return a string");
							return;
						}
					}
				}
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, new State(chunkName, env, bld, fn));
			}

			assert (chunkText != null);

			loadFromString(context, chunkName, env, chunkText);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			State state = (State) suspendedState;
			loadFromFunction(context, true, state.chunkName, state.env, state.bld, state.fn);
		}

	}

	public static class LoadFile extends AbstractLibFunction {

		private final FileSystem fileSystem;
		private final ChunkLoader loader;
		private final Object defaultEnv;

		public LoadFile(FileSystem fileSystem, ChunkLoader loader, Object defaultEnv) {
			this.fileSystem = Objects.requireNonNull(fileSystem);
			this.loader = Objects.requireNonNull(loader);
			this.defaultEnv = defaultEnv;
		}

		@Override
		protected String name() {
			return "loadfile";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {

			final ByteString fileName = args.hasNext() ? args.nextString() : null;
			final ByteString modeString = args.hasNext() ? args.nextString() : Load.DEFAULT_MODE;
			final Object env = args.hasNext() ? args.nextAny() : defaultEnv;

			boolean isStdin = fileName == null;

			// chunk name
			final String chunkName = isStdin ? "stdin" : fileName.toString();

			if (isStdin) {
				context.getReturnBuffer().setTo(null, "not supported: loadfile from stdin");
				return;
			}
			else {
				final LuaFunction fn;
				try {
					fn = loadTextChunkFromFile(fileSystem, loader, chunkName, modeString, env);
				}
				catch (LoaderException ex) {
					context.getReturnBuffer().setTo(null, ex.getLuaStyleErrorMessage());
					return;
				}

				assert (fn != null);

				context.getReturnBuffer().setTo(fn);
			}

		}

	}

	private static LuaFunction loadTextChunkFromFile(FileSystem fileSystem, ChunkLoader loader, String fileName, ByteString modeString, Object env)
			throws LoaderException {

		final LuaFunction fn;
		try {
			Path p = fileSystem.getPath(fileName);

			if (!modeString.contains((byte) 't')) {
				throw new LuaRuntimeException("attempt to load a text chunk (mode is '" + modeString + "')");
			}

			// FIXME: this is extremely wasteful!
			byte[] bytes = Files.readAllBytes(p);
			ByteString chunkText = ByteString.copyOf(bytes);
			fn = loader.loadTextChunk(new Variable(env), fileName, chunkText.toString());
		}
		catch (InvalidPathException | IOException ex) {
			throw new LoaderException(ex, fileName);
		}

		if (fn == null) {
			throw new LuaRuntimeException("loader returned nil");
		}

		return fn;
	}

	public static class DoFile extends AbstractLibFunction {

		private final FileSystem fileSystem;
		private final ChunkLoader loader;
		private final Object env;

		public DoFile(FileSystem fileSystem, ChunkLoader loader, Object env) {
			this.fileSystem = Objects.requireNonNull(fileSystem);
			this.loader = Objects.requireNonNull(loader);
			this.env = env;
		}

		@Override
		protected String name() {
			return "dofile";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			final ByteString fileName = args.hasNext() ? args.nextString() : null;

			if (fileName == null) {
				throw new UnsupportedOperationException("not supported: 'dofile' from stdin");
			}

			// TODO: we'll only be executing this function once -- add functionality to ChunkLoader to give us a "temporary" loader?

			final LuaFunction fn;
			try {
				fn = loadTextChunkFromFile(fileSystem, loader, fileName.toString(), Load.DEFAULT_MODE, env);
			}
			catch (LoaderException ex) {
				throw new LuaRuntimeException(ex.getLuaStyleErrorMessage());
			}

			try {
				Dispatch.call(context, fn);
			}
			catch (UnresolvedControlThrowable ct) {
				ct.resolve(this, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// no-op: results are already in the result buffer
		}

	}

}
