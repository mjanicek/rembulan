package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;

public interface Node {

	void accept(NodeVisitor visitor);

	Slots effect(Slots in);

}
