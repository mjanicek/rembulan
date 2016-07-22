package net.sandius.rembulan.lbc.recompiler.gen.block;

import net.sandius.rembulan.lbc.recompiler.gen.CodeVisitor;
import net.sandius.rembulan.lbc.recompiler.gen.SlotState;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntIterable;
import net.sandius.rembulan.util.IntSet;

public class Capture extends Linear implements LocalVariableEffect {

	public final IntSet indices;

	public Capture(IntIterable indices) {
		this.indices = IntSet.from(Check.notNull(indices));
	}

	@Override
	public String toString() {
		return "Capture(" + indices.toString(",") + ")";
	}

	@Override
	protected SlotState effect(SlotState in) {
		SlotState s = in;
		for (int i = 0; i < indices.length(); i++) {
			s = s.capture(indices.get(i));
		}
		return s;
	}

	@Override
	public void emit(CodeVisitor visitor) {
		visitor.visitCapture(this, inSlots(), indices);
	}

}
