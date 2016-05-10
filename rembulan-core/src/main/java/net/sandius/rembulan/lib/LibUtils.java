package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Value;
import net.sandius.rembulan.core.impl.Varargs;
import net.sandius.rembulan.util.Check;

public class LibUtils {

	public static Table init(LuaState state, Lib lib) {
		Check.notNull(state);
		Table env = state.newTable(0, 0);
		lib.installInto(state, env);
		return env;
	}

	public static void setIfNonNull(Table table, String key, Object value) {
		Check.notNull(table);
		if (value != null) {
			table.rawset(key, value);
		}
	}

	@Deprecated
	public static <T> T checkArgumentOrNil(Object arg, int index, Class<T> clazz) {
		if (arg != null && clazz.isAssignableFrom(arg.getClass())) {
			return (T) arg ;
		}
		else if (arg == null) {
			return null;
		}
		else {
			throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '?' (? or nil expected, got " + Value.typeOf(arg).name + ")");
		}
	}

	@Deprecated
	public static <T> T checkArgument(Object arg, int index, Class<T> clazz) {
		if (arg != null && clazz.isAssignableFrom(arg.getClass())) {
			return (T) arg ;
		}
		else {
			throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '?' (? expected, got " + Value.typeOf(arg).name + ")");
		}
	}

	public static Object checkValue(String name, Object[] args, int index) {
		if (index < args.length) {
			return args[index];
		}
		else {
			throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '" + name + "' (value expected)");
		}
	}

	// FIXME: clean this up: redundant code!
	public static int checkInteger(String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];

			if (arg instanceof Number) {
				Long l = Conversions.numberAsLong((Number) arg);
				if (l != null) {
					long ll = l;
					if (ll >= Integer.MIN_VALUE && ll <= Integer.MAX_VALUE) {
						return (int) ll;
					}
				}

				throw new IllegalArgumentException("number has no integer representation");
			}
			else {
				what = Value.typeOf(arg).name;
			}
		}
		else {
			what = "no value";
		}
		throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '" + name + "' (integer expected, got " + what + ")");
	}

	// FIXME: clean this up: redundant code!
	public static int checkRange(String name, Object[] args, int index, String rangeName, int min, int max) {
		final String what;
		Object o = Varargs.getElement(args, index);
		Number n = Conversions.objectAsNumber(o);

		if (n != null) {
			Integer i = Conversions.numberAsInt(n);
			if (i != null) {
				int ii = i;
				if (ii >= min && ii <= max) {
					return ii;
				}
				else {
					throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '" + name + "' (" + rangeName + " out of range)");
				}
			}
			else {
				throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '" + name + "' (number has no integer representation");
			}
		}
		else {
			what = Value.typeOf(o).name;
		}

		throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '" + name + "' (number expected, got " + what + ")");
	}

	// FIXME: clean this up: redundant code!
	public static String checkString(String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			if (arg instanceof String) {
				return (String) arg;
			}
			else {
				what = Value.typeOf(arg).name;
			}
		}
		else {
			what = "no value";
		}
		throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '" + name + "' (string expected, got " + what + ")");
	}


	// FIXME: clean this up: redundant code!
	public static Table checkTable(String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			if (arg instanceof Table) {
				return (Table) arg;
			}
			else {
				what = Value.typeOf(arg).name;
			}
		}
		else {
			what = "no value";
		}
		throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '" + name + "' (table expected, got " + what + ")");
	}

	// FIXME: clean this up: redundant code!
	public static Function checkFunction(String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			if (arg instanceof Function) {
				return (Function) arg;
			}
			else {
				what = Value.typeOf(arg).name;
			}
		}
		else {
			what = "no value";
		}
		throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '" + name + "' (function expected, got " + what + ")");
	}

	// FIXME: clean this up: redundant code!
	public static Coroutine checkCoroutine(String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			if (arg instanceof Coroutine) {
				return (Coroutine) arg;
			}
			else {
				what = Value.typeOf(arg).name;
			}
		}
		else {
			what = "no value";
		}
		throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '" + name + "' (coroutine expected, got " + what + ")");
	}

}
