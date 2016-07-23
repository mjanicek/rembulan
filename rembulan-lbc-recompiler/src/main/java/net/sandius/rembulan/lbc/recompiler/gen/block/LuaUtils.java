package net.sandius.rembulan.lbc.recompiler.gen.block;

import net.sandius.rembulan.compiler.analysis.NumericOperationType;
import net.sandius.rembulan.compiler.analysis.StaticMathImplementation;
import net.sandius.rembulan.compiler.analysis.types.Type;
import net.sandius.rembulan.compiler.analysis.types.TypeSeq;
import net.sandius.rembulan.lbc.OpCode;
import net.sandius.rembulan.lbc.recompiler.gen.PrototypeContext;
import net.sandius.rembulan.lbc.recompiler.gen.SlotState;
import net.sandius.rembulan.util.ReadOnlyArray;

// TODO: get rid of this, move the methods to more appropriate places
public abstract class LuaUtils {

	private LuaUtils() {
		// not to be instantiated
	}

	public static int registerOrConst(int i) {
		return OpCode.isK(i) ? -1 - OpCode.indexK(i) : i;
	}

	public static TypeSeq argTypesFromSlots(SlotState s, int from, int count) {
		int num = count > 0 ? count - 1 : s.varargPosition() - from;

		Type[] args = new Type[num];
		for (int i = 0; i < num; i++) {
			args[i] = s.typeAt(from + i);
		}

		return TypeSeq.of(ReadOnlyArray.wrap(args), count <= 0);
	}

	public static Type slotType(PrototypeContext context, SlotState slots, int rk) {
		return rk < 0 ? context.constType(-rk - 1) : slots.typeAt(rk);
	}

	@Deprecated
	public static NumericOperationType loopType(Type index, Type limit, Type step) {
		NumericOperationType ot = StaticMathImplementation.MAY_BE_INTEGER.opType(index, step);

		if (ot == NumericOperationType.Any) {
			// unknown types: will however be converted to numbers at execution time
			ot = NumericOperationType.Number;
		}

		return ot;
	}

	public static String numOpTypeToSuffix(NumericOperationType t) {
		switch (t) {
			case Integer: return "_i";
			case Float:   return "_f";
			case Number:  return "_N";
			case Any:
			default:      return "";
		}
	}

}
