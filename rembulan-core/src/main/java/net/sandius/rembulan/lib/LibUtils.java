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

	public static Object checkValue(String name, Object[] args, int index) {
		if (index < args.length) {
			return args[index];
		}
		else {
			throw new BadArgumentException((index + 1), name, "value expected");
		}
	}

	// FIXME: clean this up: redundant code!
	public static Number checkNumber(String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			Number n = Conversions.objectAsNumber(arg);
			if (n != null) {
				return n;
			}
			else {
				what = Value.typeOf(arg).name;
			}
		}
		else {
			what = "no value";
		}
		throw new BadArgumentException((index + 1), name, "number expected, got " + what);
	}

	// FIXME: clean this up: redundant code!
	public static int checkInt(String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			Number n = Conversions.objectAsNumber(arg);
			if (n != null) {
				Long l = Conversions.numberAsLong(n);
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
		throw new BadArgumentException((index + 1), name, "number expected, got " + what);
	}

	// FIXME: clean this up: redundant code!
	public static long checkInteger(String name, Object[] args, int index) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			Number n = Conversions.objectAsNumber(arg);
			if (n != null) {
				Long l = Conversions.numberAsLong(n);
				if (l != null) {
					return l;
				}
				else {
					throw new IllegalArgumentException("number has no integer representation");
				}
			}
			else {
				what = Value.typeOf(arg).name;
			}
		}
		else {
			what = "no value";
		}
		throw new BadArgumentException((index + 1), name, "number expected, got " + what);
	}

	// FIXME: clean this up: redundant code!
	public static int checkRange(String name, Object[] args, int index, String rangeName, int min, int max) {
		final String what;

		if (index < args.length) {
			Object o = args[index];
			Number n = Conversions.objectAsNumber(o);

			if (n != null) {
				Integer i = Conversions.numberAsInt(n);
				if (i != null) {
					int ii = i;
					if (ii >= min && ii <= max) {
						return ii;
					}
					else {
						throw new BadArgumentException((index + 1), name, rangeName + " out of range");
					}
				}
				else {
					throw new BadArgumentException((index + 1), name, "number has no integer representation");
				}
			}
			else {
				what = Value.typeOf(o).name;
			}
		}
		else {
			what = "no value";
		}

		throw new BadArgumentException((index + 1), name, "number expected, got " + what);
	}

	// FIXME: clean this up: redundant code!
	public static String checkString(String name, Object[] args, int index, boolean strict) {
		final String what;
		if (index < args.length) {
			Object arg = args[index];
			if (strict) {
				if (arg instanceof String) {
					return (String) arg;
				}
			}
			else {
				String s = Conversions.objectAsString(arg);
				if (s != null) {
					return s;
				}
			}

			// not a string!
			what = Value.typeOf(arg).name;
		}
		else {
			what = "no value";
		}
		throw new BadArgumentException((index + 1), name, "string expected, got " + what);
	}

	public static String checkString(String name, Object[] args, int index) {
		return checkString(name, args, index, false);
	}

	public static Table checkTableOrNil(String name, Object[] args, int index) {
		if (index < args.length) {
			Object arg = args[index];
			if (arg instanceof Table) {
				return (Table) arg;
			}
			else if (arg == null) {
				return null;
			}
		}
		throw new BadArgumentException((index + 1), name, "nil or table expected");
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
		throw new BadArgumentException((index + 1), name, "table expected, got " + what);
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
		throw new BadArgumentException((index + 1), name, "function expected, got " + what);
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
		throw new BadArgumentException((index + 1), name, "coroutine expected, got " + what);
	}

}
