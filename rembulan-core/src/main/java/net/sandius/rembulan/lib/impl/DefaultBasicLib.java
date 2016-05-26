package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.LuaType;
import net.sandius.rembulan.core.AssertionFailedException;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.IllegalOperationAttemptException;
import net.sandius.rembulan.core.LuaRuntimeException;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.ProtectedResumable;
import net.sandius.rembulan.core.RawOperators;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Value;
import net.sandius.rembulan.core.impl.Function1;
import net.sandius.rembulan.core.impl.Function2;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
import net.sandius.rembulan.core.impl.Varargs;
import net.sandius.rembulan.lib.BasicLib;
import net.sandius.rembulan.lib.LibUtils;
import net.sandius.rembulan.util.Check;

import java.io.PrintStream;

public class DefaultBasicLib extends BasicLib {

	private final Print print;

	public DefaultBasicLib(PrintStream out) {
		this.print = new Print(out);
	}

	@Override
	public String __VERSION() {
		return "Lua 5.3";
	}

	@Override
	public Function _print() {
		return print;
	}

	@Override
	public Function _type() {
		return Type.INSTANCE;
	}

	@Override
	public Function _next() {
		return Next.INSTANCE;
	}

	@Override
	public Function _pairs() {
		return Pairs.INSTANCE;
	}

	@Override
	public Function _ipairs() {
		return IPairs.INSTANCE;
	}

	@Override
	public Function _tostring() {
		return ToString.INSTANCE;
	}

	@Override
	public Function _tonumber() {
		return ToNumber.INSTANCE;
	}

	@Override
	public Function _error() {
		return Error.INSTANCE;
	}

	@Override
	public Function _assert() {
		return Assert.INSTANCE;
	}

	@Override
	public Function _getmetatable() {
		return GetMetatable.INSTANCE;
	}

	@Override
	public Function _setmetatable() {
		return SetMetatable.INSTANCE;
	}

	@Override
	public Function _pcall() {
		return PCall.INSTANCE;
	}

	@Override
	public Function _xpcall() {
		return XPCall.INSTANCE;
	}

	@Override
	public Function _rawequal() {
		return RawEqual.INSTANCE;
	}

	@Override
	public Function _rawget() {
		return RawGet.INSTANCE;
	}

	@Override
	public Function _rawset() {
		return RawSet.INSTANCE;
	}

	@Override
	public Function _rawlen() {
		return RawLen.INSTANCE;
	}

	@Override
	public Function _select() {
		return Select.INSTANCE;
	}

	@Override
	public Function _collectgarbage() {
		return null;  // TODO
	}

	@Override
	public Function _dofile() {
		return null;  // TODO
	}

	@Override
	public Function _load() {
		return null;  // TODO
	}

	@Override
	public Function _loadfile() {
		return null;  // TODO
	}


	public static class Print extends FunctionAnyarg {

		private final PrintStream out;

		public Print(PrintStream out) {
			this.out = Check.notNull(out);
		}

		private void run(ExecutionContext context, Object[] args) throws ControlThrowable {
			for (int i = 0; i < args.length; i++) {
				Object a = args[i];
				try {
					Dispatch.call(context, ToString.INSTANCE, a);
				}
				catch (ControlThrowable ct) {
					ct.push(this, Varargs.from(args, i + 1));
					throw ct;
				}

				Object s = context.getObjectSink()._0();
				if (s instanceof String) {
					out.print(s);
				}
				else {
					throw new LuaRuntimeException("error calling 'print' ('tostring' must return a string to 'print')");
				}

				out.print(ToString.toString(args[i]));
				if (i + 1 < args.length) {
					out.print('\t');
				}
			}
			out.println();

			// returning nothing
			context.getObjectSink().setTo();
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			run(context, args);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			run(context, (Object[]) suspendedState);
		}

	}

	public static class Type extends Function1 {

		public static final Type INSTANCE = new Type();

		@Override
		public void invoke(ExecutionContext context, Object arg) throws ControlThrowable {
			LuaType tpe = Value.typeOf(arg);
			context.getObjectSink().setTo(tpe.name);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Next extends FunctionAnyarg {

		public static final Next INSTANCE = new Next();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Table table = LibUtils.checkTable("next", args, 0);
			Object index = Varargs.getElement(args, 1);

			final Object nxt;

			if (index != null) {
				nxt = table.nextIndex(index);
			}
			else {
				nxt = table.initialIndex();
			}

			if (nxt == null) {
				// we've reached the end
				context.getObjectSink().setTo(null);
			}
			else {
				Object value = table.rawget(nxt);
				context.getObjectSink().setTo(nxt, value);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class INext extends FunctionAnyarg {

		public static final INext INSTANCE = new INext();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Table table = LibUtils.checkTable("inext", args, 0);
			int index = LibUtils.checkInt("inext", args, 1);

			index += 1;

			Object o = table.rawget(index);
			if (o != null) {
				context.getObjectSink().setTo(index, o);
			}
			else {
				context.getObjectSink().setTo(null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Pairs extends FunctionAnyarg {

		public static final Pairs INSTANCE = new Pairs();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Table t = LibUtils.checkTable("pairs", args, 0);
			Object metamethod = Metatables.getMetamethod(context.getState(), "__pairs", t);

			if (metamethod != null) {
				try {
					Dispatch.call(context, metamethod, t);
				}
				catch (ControlThrowable ct) {
					ct.push(this, null);
					throw ct;
				}

				ObjectSink os = context.getObjectSink();
				os.setTo(os._0(), os._1(), os._2());
			}
			else {
				ObjectSink os = context.getObjectSink();
				os.setTo(Next.INSTANCE, t, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			ObjectSink os = context.getObjectSink();
			os.setTo(os._0(), os._1(), os._2());
		}

	}

	public static class IPairs extends FunctionAnyarg {

		public static final IPairs INSTANCE = new IPairs();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Table t = LibUtils.checkTable("ipairs", args, 0);
			context.getObjectSink().setTo(INext.INSTANCE, t, 0L);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class ToString extends Function1 {

		public static final ToString INSTANCE = new ToString();

		public static String toString(Object o) {
			return Conversions.objectToString(o);
		}

		@Override
		public void invoke(ExecutionContext context, Object arg) throws ControlThrowable {
			Object meta = Metatables.getMetamethod(context.getState(), "__tostring", arg);
			if (meta != null) {
				try {
					Dispatch.call(context, meta, arg);
				}
				catch (ControlThrowable ct) {
					ct.push(this, null);
					throw ct;
				}

				// resume
				resume(context, null);
			}
			else {
				// no metamethod, just call the default toString
				String s = toString(arg);
				context.getObjectSink().setTo(s);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			// trim to single value
			Object result = context.getObjectSink()._1();
			context.getObjectSink().setTo(result);
		}

	}

	public static class ToNumber extends FunctionAnyarg {

		public static final ToNumber INSTANCE = new ToNumber();

		private static final int MIN_BASE = 2;
		private static final int MAX_BASE = 36;

		public static Long toNumber(String s, int base) {
			Check.notNull(s);
			Check.inRange(base, MIN_BASE, MAX_BASE);

			s = s.trim();

			int idx = 0;

			// empty!
			if (s.isEmpty()) {
				return null;
			}

			// sign?
			final boolean pos;

			{
				final char c = s.charAt(idx);

				if (c == '-') {
					pos = false;
					idx++;
				}
				else if (c == '+') {
					pos = true;
					idx++;
				}
				else {
					pos = true;
				}
			}

			// no digits!
			if (idx >= s.length()) {
				return null;
			}

			long n = 0;

			// digits
			while (idx < s.length()) {
				final char c = s.charAt(idx);
				final int digit;

				if (c >= '0' && c <= '9') {
					digit = c - '0';
				}
				else if (c >= 'a' && c <= 'z') {
					digit = 10 + c - 'a';
				}
				else if (c >= 'A' && c <= 'Z') {
					digit = 10 + c - 'A';
				}
				else {
					// non-alphanumeric character
					return null;
				}

				if (digit >= base) {
					// doesn't fit in the base
					return null;
				}

				n = n * base + digit;
				idx++;
			}

			return pos ? n : -n;
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			if (Varargs.getElement(args, 1) == null) {
				// no base
				Object o = LibUtils.checkValue("tonumber", args, 0);
				Number n = Conversions.objectAsNumber(o);
				context.getObjectSink().setTo(n);
			}
			else {
				String s = LibUtils.checkString("tonumber", args, 0);
				int base = LibUtils.checkRange("tonumber", args, 1, "base", MIN_BASE, MAX_BASE);
				context.getObjectSink().setTo(toNumber(s, base));
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}


	public static class GetMetatable extends Function1 {

		public static final GetMetatable INSTANCE = new GetMetatable();

		@Override
		public void invoke(ExecutionContext context, Object arg) throws ControlThrowable {
			Object meta = Metatables.getMetamethod(context.getState(), "__metatable", arg);

			Object result = meta != null
					? meta  // __metatable field present, return its value
					: Metatables.getMetatable(context.getState(), arg);  // return the entire metatable

			context.getObjectSink().setTo(result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class SetMetatable extends Function2 {

		public static final SetMetatable INSTANCE = new SetMetatable();

		@Override
		public void invoke(ExecutionContext context, Object arg1, Object arg2) throws ControlThrowable {
			Table t = LibUtils.checkArgument(arg1, 0, Table.class);
			Table mt = LibUtils.checkArgumentOrNil(arg2, 1, Table.class);

			if (Metatables.getMetamethod(context.getState(), "__metatable", t) != null) {
				throw new IllegalOperationAttemptException("cannot change a protected metatable");
			}
			else {
				t.setMetatable(mt);
				context.getObjectSink().setTo(t);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Error extends Function2 {

		public static final Error INSTANCE = new Error();

		@Override
		public void invoke(ExecutionContext context, Object arg1, Object arg2) throws ControlThrowable {
			// TODO: handle levels
			throw new LuaRuntimeException(arg1);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Assert extends FunctionAnyarg {

		public static final Assert INSTANCE = new Assert();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			if (Conversions.objectToBoolean(Varargs.getElement(args, 0))) {
				context.getObjectSink().setToArray(args);
			}
			else {
				final AssertionFailedException ex;
				if (args.length > 1) {
					// message is defined
					Object message = args[1];
					String stringMessage = Conversions.objectAsString(message);
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

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class PCall extends FunctionAnyarg implements ProtectedResumable {

		public static final PCall INSTANCE = new PCall();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Object callTarget = Varargs.getElement(args, 0);
			Object[] callArgs = Varargs.from(args, 1);

			try {
				Dispatch.call(context, callTarget, callArgs);
			}
			catch (ControlThrowable ct) {
				ct.push(this, null);
				throw ct;
			}
			catch (Exception ex) {
				context.getObjectSink().setTo(Boolean.FALSE, Conversions.throwableToObject(ex));  // failure
				return;
			}

			context.getObjectSink().prepend(Boolean.TRUE);  // success
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			// success
			context.getObjectSink().prepend(Boolean.TRUE);
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ControlThrowable {
			context.getObjectSink().setTo(Boolean.FALSE, error);
		}

	}

	public static class XPCall extends FunctionAnyarg implements ProtectedResumable {

		public static final int MAX_DEPTH = 220;  // 220 in PUC-Lua 5.3

		public static final XPCall INSTANCE = new XPCall();

		private static class SavedState {
			public final Function handler;
			public final int depth;

			private SavedState(Function handler, int depth) {
				this.handler = handler;
				this.depth = depth;
			}
		}

		private void prependTrue(ExecutionContext context) {
			context.getObjectSink().prepend(Boolean.TRUE);
		}

		private void prependFalseAndPad(ExecutionContext context) {
			ObjectSink os = context.getObjectSink();
			if (os.size() == 0) {
				// if empty, pad with a dummy nil value
				os.setTo(Boolean.FALSE, null);
			}
			else {
				// just prepend false
				os.prepend(Boolean.FALSE);
			}
		}

		private void handleError(ExecutionContext context, Function handler, int depth, Object errorObject) throws ControlThrowable {
			// we want to be able to handle nil error objects, so we need a separate flag
			boolean isError = true;

			while (isError && depth < MAX_DEPTH) {
				depth += 1;

				try {
					Dispatch.call(context, handler, errorObject);
					isError = false;
				}
				catch (ControlThrowable ct) {
					ct.push(this, new SavedState(handler, depth));
					throw ct;
				}
				catch (Exception e) {
					errorObject = Conversions.throwableToObject(e);
					isError = true;
				}
			}

			if (!isError) {
				prependFalseAndPad(context);
			}
			else {
				// depth must be >= MAX_DEPTH
				context.getObjectSink().setTo(Boolean.FALSE, "error in error handling");
			}
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Function handler = LibUtils.checkFunction("xpcall", args, 1);
			Object callTarget = LibUtils.checkValue("xpcall", args, 0);
			Object[] callArgs = Varargs.from(args, 2);

			Object errorObject = null;
			boolean isError = false;  // need to distinguish nil error objects from no-error

			try {
				Dispatch.call(context, callTarget, callArgs);
			}
			catch (ControlThrowable ct) {
				ct.push(this, new SavedState(handler, 0));
			}
			catch (Exception e) {
				errorObject = Conversions.throwableToObject(e);
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
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			SavedState ss = (SavedState) suspendedState;
			if (ss.depth == 0) {
				prependTrue(context);
			}
			else {
				prependFalseAndPad(context);
			}
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ControlThrowable {
			SavedState ss = (SavedState) suspendedState;
			handleError(context, ss.handler, ss.depth, error);
		}

	}

	public static class RawEqual extends FunctionAnyarg {

		public static final RawEqual INSTANCE = new RawEqual();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Object a = LibUtils.checkValue("rawequal", args, 0);
			Object b = LibUtils.checkValue("rawequal", args, 1);

			context.getObjectSink().setTo(RawOperators.raweq(a, b));
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class RawGet extends FunctionAnyarg {

		public static final RawGet INSTANCE = new RawGet();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Table table = LibUtils.checkTable("rawget", args, 0);
			Object key = LibUtils.checkValue("rawget", args, 1);

			context.getObjectSink().setTo(table.rawget(key));
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class RawSet extends FunctionAnyarg {

		public static final RawSet INSTANCE = new RawSet();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Table table = LibUtils.checkTable("rawset", args, 0);
			Object key = LibUtils.checkValue("rawset", args, 1);
			Object value = LibUtils.checkValue("rawset", args, 2);

			table.rawset(key, value);
			context.getObjectSink().setTo(table);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class RawLen extends Function1 {

		public static final RawLen INSTANCE = new RawLen();

		@Override
		public void invoke(ExecutionContext context, Object arg1) throws ControlThrowable {
			final long result;

			if (arg1 instanceof Table) {
				Table table = (Table) arg1;
				result = table.rawlen();
			}
			else if (arg1 instanceof String) {
				String s = (String) arg1;
				result = RawOperators.stringLen(s);
			}
			else {
				throw new IllegalArgumentException("bad argument #1 to 'rawlen' (table or string expected)");
			}

			context.getObjectSink().setTo(result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Select extends FunctionAnyarg {

		public static final Select INSTANCE = new Select();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Object index = LibUtils.checkValue("select", args, 0);

			if (index instanceof String && ((String) index).startsWith("#")) {
				// return the number of remaining args
				context.getObjectSink().setTo((long) args.length - 1);
			}
			else {
				int idx = LibUtils.checkRange("select", args, 0, "index", -args.length + 1, Integer.MAX_VALUE);

				int from = idx >= 0
						? idx  // from the beginning
						: args.length + idx;  // idx < 0: from the end (-1 is the last index)

				if (from < 1) {
					throw new IllegalArgumentException("bad argument #1 to 'select' (index out of range)");
				}

				context.getObjectSink().setToArray(Varargs.from(args, from));
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

}
