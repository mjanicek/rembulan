package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class BinOp extends BodyNode {

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
	private final Val dest;
	private final Val left;
	private final Val right;

	public BinOp(Op op, Val dest, Val left, Val right) {
		this.op = Check.notNull(op);
		this.dest = Check.notNull(dest);
		this.left = Check.notNull(left);
		this.right = Check.notNull(right);
	}

	public Op op() {
		return op;
	}

	public Val dest() {
		return dest;
	}

	public Val left() {
		return left;
	}

	public Val right() {
		return right;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
