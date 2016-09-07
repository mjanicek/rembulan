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
import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.AssertionFailedException;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.BasicLib;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.runtime.Dispatch;
import net.sandius.rembulan.runtime.IllegalOperationAttemptException;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.UnresolvedControlThrowable;
import net.sandius.rembulan.util.Check;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

public class DefaultBasicLib extends BasicLib {

	private final LuaFunction _print;
	private final LuaFunction _dofile;
	private final LuaFunction _load;
	private final LuaFunction _loadfile;

	public DefaultBasicLib(PrintStream out, ChunkLoader loader, Object defaultEnv) {
		this._print = new Print(out);
		this._dofile = new UnimplementedFunction("dofile");  // TODO

		if (loader != null) {
			this._load = new Load(loader, defaultEnv);
		}
		else {
			// no loader supplied
			this._load = null;
		}

		this._loadfile = new UnimplementedFunction("loadfile");  // TODO
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
					throw ct.push(this, Arrays.copyOfRange(args, i + 1, args.length));
				}

				Object s = context.getReturnBuffer().get0();
				if (s instanceof String) {
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
			String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(args.nextAny());
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
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Table table = args.nextTable();
			int index = args.nextInt();

			index += 1;

			Object o = table.rawget(index);
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
			Object metamethod = Metatables.getMetamethod(context.getState(), MT_PAIRS, t);

			if (metamethod != null) {
				try {
					Dispatch.call(context, metamethod, t);
				}
				catch (UnresolvedControlThrowable ct) {
					throw ct.push(this, null);
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

			Object meta = Metatables.getMetamethod(context.getState(), MT_TOSTRING, arg);
			if (meta != null) {
				try {
					Dispatch.call(context, meta, arg);
				}
				catch (UnresolvedControlThrowable ct) {
					throw ct.push(this, null);
				}

				// resume
				resume(context, null);
			}
			else {
				// no metamethod, just call the default toString
				String s = Conversions.toHumanReadableString(arg);
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

		public static Long toNumber(String s, int base) {
			try {
				return Long.parseLong(s.trim(), base);
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
				String s = args.nextStrictString();
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
			Object meta = Metatables.getMetamethod(context.getState(), MT_METATABLE, arg);

			Object result = meta != null
					? meta  // __metatable field present, return its value
					: context.getState().getMetatable(arg);  // return the entire metatable

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

			if (Metatables.getMetamethod(context.getState(), MT_METATABLE, t) != null) {
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
					String stringMessage = Conversions.stringValueOf(message);
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
				throw ct.push(this, null);
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
					throw ct.push(this, new SavedState(handler, depth));
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
				ct.push(this, new SavedState(handler, 0));
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

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Object index = args.peekOrNil();

			if (index instanceof String && ((String) index).startsWith("#")) {
				// return the number of remaining args
				int count = args.tailSize() - 1;
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
				chunkName = chunk instanceof String
						? "[string \"" + (String) chunk + "\"]"
						: "=(load)";
			}

			// mode
			final String modeString = args.hasNext()
					? args.nextString()
					: "bt";

			final Object env = args.hasNext()
					? args.nextAny()
					: defaultEnv;

			// TODO: binary chunks

			if (!modeString.contains("t")) {
				context.getReturnBuffer().setTo(null, "attempt to load a text chunk (mode is '" + modeString + "')");
			}
			else {
				if (chunk instanceof String) {
					loadFromString(context, chunkName, env, (String) chunk);
				}
				else {
					LuaFunction fn = (LuaFunction) chunk;
					loadFromFunction(context, false, chunkName, env, new StringBuilder(), fn);
				}
			}

		}

		private void loadFromString(ExecutionContext context, String chunkName, Object env, String chunkText) {
			final LuaFunction fn;
			try {
				fn = loader.loadTextChunk(new Variable(env), chunkName, chunkText);
			}
			catch (LoaderException ex) {
				context.getReturnBuffer().setTo(null, Conversions.toErrorObject(ex));
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
			public final StringBuilder bld;
			public final LuaFunction fn;

			private State(String chunkName, Object env, StringBuilder bld, LuaFunction fn) {
				this.chunkName = chunkName;
				this.env = env;
				this.bld = bld;
				this.fn = fn;
			}

		}

		private void loadFromFunction(ExecutionContext context, boolean resuming, String chunkName, Object env, StringBuilder bld, LuaFunction fn)
				throws ResolvedControlThrowable {

			String chunkText = null;

			try {
				while (chunkText == null) {
					if (!resuming) {
						Dispatch.call(context, fn);
					}

					resuming = false;

					Object o = context.getReturnBuffer().get0();
					if (o == null) {
						chunkText = bld.toString();
					}
					else {
						String s = Conversions.stringValueOf(o);
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
				throw ct.push(this, new State(chunkName, env, bld, fn));
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

}
