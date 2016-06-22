package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.IRVisitor;

import java.util.Iterator;

public abstract class BlocksVisitor extends IRVisitor {

	public void visit(Blocks blocks) {
		Iterator<BasicBlock> it = blocks.blockIterator();
		while (it.hasNext()) {
			BasicBlock b = it.next();
			visit(b);
		}
	}

	public void visit(BasicBlock block) {
		visit(block.label());
		for (IRNode n : block.body()) {
			n.accept(this);
		}
		block.end().accept(this);
	}

}
