package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.MetatableProvider;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.PlainValueTypeNamer;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.ValueTypeNamer;
import net.sandius.rembulan.util.Check;

public class LibUtils {

	public static final String MT_NAME = "__name";

	public static Table init(LuaState state, Lib lib) {
		Check.notNull(state);
		Table env = state.newTable();
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

	public static class NameMetamethodValueTypeNamer implements ValueTypeNamer {

		private final MetatableProvider metatableProvider;

		public NameMetamethodValueTypeNamer(MetatableProvider metatableProvider) {
			this.metatableProvider = Check.notNull(metatableProvider);
		}

		@Override
		public String typeNameOf(Object instance) {
			Object nameField = Metatables.getMetamethod(metatableProvider, MT_NAME, instance);
			if (nameField instanceof String) {
				return (String) nameField;
			}
			else {
				return PlainValueTypeNamer.INSTANCE.typeNameOf(instance);
			}
		}

	}

	private static Object getArg(Object[] args, int index, String expected) {
		if (index < args.length) {
			return args[index];
		}
		else {
			throw new UnexpectedArgumentException(expected, "no value");
		}
	}

	private static int verifyRange(long value, int min, int max, String name) {
		if (value >= min && value <= max) {
			return (int) value;
		}
		else {
			throw new IndexOutOfBoundsException(name + " out of range");
		}
	}

	public static Number checkNumber(ValueTypeNamer namer, String name, Object[] args, int index) {
		try {
			Object arg = getArg(args, index, "number");
			Number n = Conversions.numericalValueOf(arg);
			if (n != null) {
				return n;
			}
			else {
				throw new UnexpectedArgumentException("number", namer.typeNameOf(arg));
			}
		}
		catch (RuntimeException ex) {
			throw new BadArgumentException((index + 1), name, ex);
		}
	}

	public static int checkInt(ValueTypeNamer namer, String name, Object[] args, int index) {
		try {
			Object arg = getArg(args, index, "number");
			Number n = Conversions.numericalValueOf(arg);
			if (n != null) {
				return (int) Conversions.toIntegerValue(n);
			}
			else {
				throw new UnexpectedArgumentException("number", namer.typeNameOf(arg));
			}
		}
		catch (RuntimeException ex) {
			throw new BadArgumentException((index + 1), name, ex);
		}
	}

	public static long checkInteger(ValueTypeNamer namer, String name, Object[] args, int index) {
		try {
			Object arg = getArg(args, index, "number");
			Long l = Conversions.integerValueOf(arg);
			if (l != null) {
				return l.longValue();
			}
			else {
				throw new UnexpectedArgumentException("number", namer.typeNameOf(arg));
			}
		}
		catch (RuntimeException ex) {
			throw new BadArgumentException((index + 1), name, ex);
		}
	}

	public static int checkRange(ValueTypeNamer namer, String name, Object[] args, int index, String rangeName, int min, int max) {
		try {
			Object o = getArg(args, index, "number");
			Number n = Conversions.numericalValueOf(o);

			if (n != null) {
				return verifyRange(Conversions.toIntegerValue(n), min, max, rangeName);
			}
			else {
				throw new UnexpectedArgumentException("number", namer.typeNameOf(o));
			}
		}
		catch (RuntimeException ex) {
			throw new BadArgumentException((index + 1), name, ex);
		}
	}

	public static String checkStrictString(ValueTypeNamer namer, String name, Object[] args, int index) {
		try {
			Object arg = getArg(args, index, "string");
			if (arg instanceof String) {
				return (String) arg;
			}
			else {
				throw new UnexpectedArgumentException("string", namer.typeNameOf(arg));
			}
		}
		catch (RuntimeException ex) {
			throw new BadArgumentException((index + 1), name, ex);
		}
	}

	public static String checkStringValue(ValueTypeNamer namer, String name, Object[] args, int index) {
		try {
			Object arg = getArg(args, index, "string");
			String s = Conversions.stringValueOf(arg);
			if (s != null) {
				return s;
			}
			else {
				throw new UnexpectedArgumentException("string", namer.typeNameOf(arg));
			}
		}
		catch (RuntimeException ex) {
			throw new BadArgumentException((index + 1), name, ex);
		}
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

	public static Table checkTable(ValueTypeNamer namer, String name, Object[] args, int index) {
		try {
			Object arg = getArg(args, index, "table");
			if (arg instanceof Table) {
				return (Table) arg;
			}
			else {
				throw new UnexpectedArgumentException("table", namer.typeNameOf(arg));
			}
		}
		catch (RuntimeException ex) {
			throw new BadArgumentException((index + 1), name, ex);
		}
	}

	public static Function checkFunction(ValueTypeNamer namer, String name, Object[] args, int index) {
		try {
			Object arg = getArg(args, index, "function");
			if (arg instanceof Function) {
				return (Function) arg;
			}
			else {
				throw new UnexpectedArgumentException("function", namer.typeNameOf(arg));
			}
		}
		catch (RuntimeException ex) {
			throw new BadArgumentException((index + 1), name, ex);
		}
	}

	public static Coroutine checkCoroutine(ValueTypeNamer namer, String name, Object[] args, int index) {
		try {
			Object arg = getArg(args, index, "coroutine");
			if (arg instanceof Coroutine) {
				return (Coroutine) arg;
			}
			else {
				throw new UnexpectedArgumentException("coroutine", namer.typeNameOf(arg));
			}
		}
		catch (RuntimeException ex) {
			throw new BadArgumentException((index + 1), name, ex);
		}
	}

}
