package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class BinOp extends IRNode {

	public enum Op {
		ADD,
		SUB,
		MUL,
		DIV,
		MOD,
		IDIV,
		CONCAT,
		BAND,
		BOR,
		BXOR,
		SHL,
		SHR,
		EQ,
		NEQ,
		LT,
		AND,
		OR,
		POW
	}

	private final Op op;

	public BinOp(Op op) {
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
