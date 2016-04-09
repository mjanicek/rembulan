package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.CodeVisitor;
import net.sandius.rembulan.compiler.gen.SlotState;

public interface Node {

	void accept(NodeVisitor visitor);

	SlotState inSlots();

	SlotState outSlots();

	boolean pushSlots(SlotState s);

	void clearSlots();

	void emit(CodeVisitor visitor);

}
