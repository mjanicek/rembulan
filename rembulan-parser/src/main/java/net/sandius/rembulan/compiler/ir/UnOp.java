package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class UnOp extends IRNode {

	public enum Op {
		UNM,
		BNOT,
		NOT,
		LEN
	}

	private final Op op;

	public UnOp(Op op) {
		this.op = Check.notNull(op);
	}

	public Op op() {
		return op;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
