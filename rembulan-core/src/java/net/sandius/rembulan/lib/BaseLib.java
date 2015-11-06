package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaType;
import net.sandius.rembulan.core.Value;
import net.sandius.rembulan.util.Check;

import java.io.PrintStream;

public class BaseLib {

	public static final Print PRINT = new Print(System.out);
	public static final Type TYPE = new Type();
	public static final ToString TOSTRING = new ToString();

	public static class Print extends Function {

		private final PrintStream ps;

		public Print(PrintStream ps) {
			Check.notNull(ps);
			this.ps = ps;
		}

		@Override
		public Object[] invoke(Object[] args) {
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					ps.print(Value.toString(args[i]));
					if (i + 1 < args.length) {
						ps.print("\t");
					}
				}
			}

			ps.println("\n");

			return null;
		}

	}

	public static class Type extends Function {

		@Override
		public Object[] invoke(Object[] args) {
			Check.numOfArgs(args, 1);
			return new Object[] { LuaType.typeOf(args[0]).name };
		}

	}

	public static class ToString extends Function {

		@Override
		public Object[] invoke(Object[] args) {
			Check.numOfArgs(args, 1);
			return new Object[] { Value.toString(args[0]) };
		}
	}

}
