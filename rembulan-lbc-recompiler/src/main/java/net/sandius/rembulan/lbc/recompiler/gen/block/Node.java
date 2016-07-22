package net.sandius.rembulan.lbc.recompiler.gen.block;

import net.sandius.rembulan.lbc.recompiler.gen.CodeVisitor;
import net.sandius.rembulan.lbc.recompiler.gen.SlotState;

public interface Node {

	void accept(NodeVisitor visitor);

	SlotState inSlots();

	SlotState outSlots();

	boolean pushSlots(SlotState s);

	void clearSlots();

	void emit(CodeVisitor visitor);

}
