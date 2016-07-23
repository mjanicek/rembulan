package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.compiler.IRFunc;

import java.util.Iterator;

public class CodeVisitor extends IRVisitor {

	public CodeVisitor(IRVisitor visitor) {
		super(visitor);
	}

	public CodeVisitor() {
		super();
	}

	public void visit(IRFunc func) {
		visit(func.code());
	}

	public void visit(Code code) {
		Iterator<BasicBlock> it = code.blockIterator();
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
