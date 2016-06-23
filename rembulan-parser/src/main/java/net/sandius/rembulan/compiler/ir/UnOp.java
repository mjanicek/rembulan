package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class UnOp extends BodyNode {

	public enum Op {
		UNM,
		BNOT,
		NOT,
		LEN
	}

	private final Op op;
	private final Val dest;
	private final Val arg;

	public UnOp(Op op, Val dest, Val arg) {
		this.op = Check.notNull(op);
		this.dest = Check.notNull(dest);
		this.arg = Check.notNull(arg);
	}

	public Op op() {
		return op;
	}

	public Val dest() {
		return dest;
	}

	public Val arg() {
		return arg;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
