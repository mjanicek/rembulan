package net.sandius.rembulan.lib;

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

	public static <T> T checkArgument(Object arg, int index, Class<T> clazz) {
		if (arg != null && clazz.isAssignableFrom(arg.getClass())) {
			return (T) arg ;
		}
		else {
			throw new IllegalArgumentException("bad argument #" + (index + 1) + " to '?' (? expected, got " + Value.typeOf(arg).name + ")");
		}
	}

	public static <T> T getArgument(Object[] args, int index, Class<T> clazz) {
		return checkArgument(Varargs.getElement(args, index), index, clazz);
	}

}
