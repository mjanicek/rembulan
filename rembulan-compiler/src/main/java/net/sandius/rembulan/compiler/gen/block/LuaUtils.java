package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.compiler.types.TypeSeq;
import net.sandius.rembulan.lbc.OpCode;
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

}
