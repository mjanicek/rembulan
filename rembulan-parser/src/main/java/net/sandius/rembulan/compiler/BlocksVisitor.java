package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.util.Check;

import java.util.Iterator;

public class BlocksVisitor extends IRVisitor {

	public BlocksVisitor(IRVisitor visitor) {
		super(visitor);
	}

	public BlocksVisitor() {
		super();
	}

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
