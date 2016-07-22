package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.types.Type;

import static net.sandius.rembulan.compiler.gen.LuaTypes.NUMBER;
import static net.sandius.rembulan.compiler.gen.LuaTypes.NUMBER_FLOAT;
import static net.sandius.rembulan.compiler.gen.LuaTypes.NUMBER_INTEGER;

public abstract class StaticMathImplementation {

	public static StaticMathImplementation MAY_BE_INTEGER = new MayBeInteger();

	public static StaticMathImplementation MUST_BE_FLOAT = new MustBeFloat();

	public static StaticMathImplementation MUST_BE_INTEGER = new MustBeInteger();

	public abstract NumOpType opType(Type left, Type right);

	public abstract NumOpType opType(Type arg);

	public static class MayBeInteger extends StaticMathImplementation {

		private MayBeInteger() {
			// not to be instantiated by the outside world
		}

		@Override
		public NumOpType opType(Type l, Type r) {
			if (l.isSubtypeOf(NUMBER) && r.isSubtypeOf(NUMBER)) {
				if (l.isSubtypeOf(NUMBER_INTEGER) && r.isSubtypeOf(NUMBER_INTEGER)) return NumOpType.Integer;
				else if (l.isSubtypeOf(NUMBER_FLOAT) || r.isSubtypeOf(NUMBER_FLOAT)) return NumOpType.Float;
				else return NumOpType.Number;
			}
			else {
				return NumOpType.Any;
			}
		}

		@Override
		public NumOpType opType(Type arg) {
			if (arg.isSubtypeOf(NUMBER)) {
				if (arg.isSubtypeOf(NUMBER_INTEGER)) return NumOpType.Integer;
				else if (arg.isSubtypeOf(NUMBER_FLOAT)) return NumOpType.Float;
				else return NumOpType.Number;
			}
			else {
				return NumOpType.Any;
			}
		}

	}

	public static class MustBeFloat extends StaticMathImplementation {

		private MustBeFloat() {
			// not to be instantiated by the outside world
		}

		@Override
		public NumOpType opType(Type l, Type r) {
			if (l.isSubtypeOf(NUMBER) && r.isSubtypeOf(NUMBER)) return NumOpType.Float;
			else return NumOpType.Any;
		}

		@Override
		public NumOpType opType(Type arg) {
			if (arg.isSubtypeOf(NUMBER)) return NumOpType.Float;
			else return NumOpType.Any;
		}

	}

	public static class MustBeInteger extends StaticMathImplementation {

		private MustBeInteger() {
			// not to be instantiated by the outside world
		}

		@Override
		public NumOpType opType(Type l, Type r) {
			if (l.isSubtypeOf(NUMBER) && r.isSubtypeOf(NUMBER)) return NumOpType.Integer;
			else return NumOpType.Any;
		}

		@Override
		public NumOpType opType(Type arg) {
			if (arg.isSubtypeOf(NUMBER)) return NumOpType.Integer;
			else return NumOpType.Any;
		}

	}

}
