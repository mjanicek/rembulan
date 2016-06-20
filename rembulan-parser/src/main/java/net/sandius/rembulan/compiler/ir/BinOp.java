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
		POW,
		CONCAT,
		BAND,
		BOR,
		BXOR,
		SHL,
		SHR,
		EQ,
		NEQ,
		LT,
		LE
	}

	private final Op op;
	private final Temp dest;
	private final Temp left;
	private final Temp right;

	public BinOp(Op op, Temp dest, Temp left, Temp right) {
		this.op = Check.notNull(op);
		this.dest = Check.notNull(dest);
		this.left = Check.notNull(left);
		this.right = Check.notNull(right);
	}

	public Op op() {
		return op;
	}

	public Temp dest() {
		return dest;
	}

	public Temp left() {
		return left;
	}

	public Temp right() {
		return right;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
