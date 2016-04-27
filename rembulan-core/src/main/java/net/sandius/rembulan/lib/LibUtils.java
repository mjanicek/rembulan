package net.sandius.rembulan.lib;

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
