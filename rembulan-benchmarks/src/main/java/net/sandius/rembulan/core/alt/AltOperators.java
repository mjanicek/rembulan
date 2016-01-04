package net.sandius.rembulan.core.alt;

import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.RawOperators;
import net.sandius.rembulan.core.Value;

public abstract class AltOperators {

	private AltOperators() {
		// not to be instantiated or extended
	}


	public interface MathImpl {

		Number add(Number a, Number b);

		Number sub(Number a, Number b);

		boolean lt(Number a, Number b);

	}

	protected static class IntegerMathImpl implements MathImpl {

		@Override
		public Number add(Number a, Number b) {
			return RawOperators.rawadd(a.longValue(), b.longValue());
//			return a.longValue() + b.longValue();
		}

		@Override
		public Number sub(Number a, Number b) {
			return RawOperators.rawsub(a.longValue(), b.longValue());
//			return a.longValue() - b.longValue();
		}

		@Override
		public boolean lt(Number a, Number b) {
			return RawOperators.rawlt(a.longValue(), b.longValue());
//			return a.longValue() < b.longValue();
		}

	}

	protected static class FloatMathImpl implements MathImpl {

		@Override
		public Number add(Number a, Number b) {
			return RawOperators.rawadd(a.doubleValue(), b.doubleValue());
//			return a.doubleValue() + b.doubleValue();
		}

		@Override
		public Number sub(Number a, Number b) {
			return RawOperators.rawsub(a.doubleValue(), b.doubleValue());
//			return a.doubleValue() - b.doubleValue();
		}

		@Override
		public boolean lt(Number a, Number b) {
			return RawOperators.rawlt(a.doubleValue(), b.doubleValue());
//			return a.doubleValue() < b.doubleValue();
		}

	}

	protected static final IntegerMathImpl INTEGER_MATH = new IntegerMathImpl();
	protected static final FloatMathImpl FLOAT_MATH = new FloatMathImpl();

	private static MathImpl __bin_arith_op(Number a, Number b) {
		return (Value.isFloat(a) || Value.isFloat(b)) ? FLOAT_MATH : INTEGER_MATH;
	}

	private static MathImpl __lt(Number a, Number b) {
		return (Value.isFloat(a) || Value.isFloat(b)) ? FLOAT_MATH : INTEGER_MATH;
	}

	public static Object add(LuaState state, Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			Number na = (Number) a;
			Number nb = (Number) b;
			return __bin_arith_op(na, nb).add(na, nb);
		}
		else if ((a instanceof String || a instanceof Number) || (b instanceof String || b instanceof Number)) {
			Number na = Conversions.objectAsNumber(a);
			Number nb = Conversions.objectAsNumber(b);
			if (na != null && nb != null) {
				return __bin_arith_op(na, nb).add(na, nb);
			}
		}

		return Operators.tryMetamethodCall(state, Metatables.MT_ADD, a, b);
	}

	public static Object sub(LuaState state, Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			Number na = (Number) a;
			Number nb = (Number) b;
			return __bin_arith_op(na, nb).sub(na, nb);
		}
		else if ((a instanceof String || a instanceof Number) || (b instanceof String || b instanceof Number)) {
			Number na = Conversions.objectAsNumber(a);
			Number nb = Conversions.objectAsNumber(b);
			if (na != null && nb != null) {
				return __bin_arith_op(na, nb).sub(na, nb);
			}
		}

		return Operators.tryMetamethodCall(state, Metatables.MT_SUB, a, b);
	}

	public static boolean lt(LuaState state, Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			Number na = (Number) a;
			Number nb = (Number) b;
			return __lt(na, nb).lt(na, nb);
		}
		else if (a instanceof String && b instanceof String) {
			return RawOperators.rawlt((String) a, (String) b);
		}
		else {
			return Conversions.objectToBoolean(Operators.tryMetamethodCall(state, Metatables.MT_LT, a, b));
		}
	}

	public static boolean gt(LuaState state, Object a, Object b) {
		return lt(state, b, a);
	}

}
