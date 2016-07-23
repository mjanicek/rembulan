package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.analysis.types.Type;

import static net.sandius.rembulan.compiler.analysis.types.LuaTypes.NUMBER;
import static net.sandius.rembulan.compiler.analysis.types.LuaTypes.NUMBER_FLOAT;
import static net.sandius.rembulan.compiler.analysis.types.LuaTypes.NUMBER_INTEGER;

public abstract class StaticMathImplementation {

	public static StaticMathImplementation MAY_BE_INTEGER = new MayBeInteger();

	public static StaticMathImplementation MUST_BE_FLOAT = new MustBeFloat();

	public static StaticMathImplementation MUST_BE_INTEGER = new MustBeInteger();

	public abstract NumericOperationType opType(Type left, Type right);

	public abstract NumericOperationType opType(Type arg);

	public static class MayBeInteger extends StaticMathImplementation {

		private MayBeInteger() {
			// not to be instantiated by the outside world
		}

		@Override
		public NumericOperationType opType(Type l, Type r) {
			if (l.isSubtypeOf(NUMBER) && r.isSubtypeOf(NUMBER)) {
				if (l.isSubtypeOf(NUMBER_INTEGER) && r.isSubtypeOf(NUMBER_INTEGER)) return NumericOperationType.Integer;
				else if (l.isSubtypeOf(NUMBER_FLOAT) || r.isSubtypeOf(NUMBER_FLOAT)) return NumericOperationType.Float;
				else return NumericOperationType.Number;
			}
			else {
				return NumericOperationType.Any;
			}
		}

		@Override
		public NumericOperationType opType(Type arg) {
			if (arg.isSubtypeOf(NUMBER)) {
				if (arg.isSubtypeOf(NUMBER_INTEGER)) return NumericOperationType.Integer;
				else if (arg.isSubtypeOf(NUMBER_FLOAT)) return NumericOperationType.Float;
				else return NumericOperationType.Number;
			}
			else {
				return NumericOperationType.Any;
			}
		}

	}

	public static class MustBeFloat extends StaticMathImplementation {

		private MustBeFloat() {
			// not to be instantiated by the outside world
		}

		@Override
		public NumericOperationType opType(Type l, Type r) {
			if (l.isSubtypeOf(NUMBER) && r.isSubtypeOf(NUMBER)) return NumericOperationType.Float;
			else return NumericOperationType.Any;
		}

		@Override
		public NumericOperationType opType(Type arg) {
			if (arg.isSubtypeOf(NUMBER)) return NumericOperationType.Float;
			else return NumericOperationType.Any;
		}

	}

	public static class MustBeInteger extends StaticMathImplementation {

		private MustBeInteger() {
			// not to be instantiated by the outside world
		}

		@Override
		public NumericOperationType opType(Type l, Type r) {
			if (l.isSubtypeOf(NUMBER) && r.isSubtypeOf(NUMBER)) return NumericOperationType.Integer;
			else return NumericOperationType.Any;
		}

		@Override
		public NumericOperationType opType(Type arg) {
			if (arg.isSubtypeOf(NUMBER)) return NumericOperationType.Integer;
			else return NumericOperationType.Any;
		}

	}

}
