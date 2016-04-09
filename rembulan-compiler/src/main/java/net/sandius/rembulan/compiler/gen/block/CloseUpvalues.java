package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.CodeVisitor;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.util.Check;

public class CloseUpvalues extends Linear implements LocalVariableEffect {

	public final int fromIndex;

	public CloseUpvalues(int fromIndex) {
		Check.nonNegative(fromIndex);
		this.fromIndex = fromIndex;
	}

	@Override
	public String toString() {
		return "CloseUpvalues(" + fromIndex + ")";
	}

	@Override
	public SlotState effect(SlotState in) {
		SlotState s = in;
		for (int i = fromIndex; i < in.size(); i++) {
			s = s.freshen(i);
		}

		return s;
	}

	@Override
	public void emit(CodeVisitor visitor) {
		visitor.visitCloseUpvalues(this, inSlots(), fromIndex);
	}

}
