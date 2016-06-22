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
	private final Temp dest;
	private final Temp arg;

	public UnOp(Op op, Temp dest, Temp arg) {
		this.op = Check.notNull(op);
		this.dest = Check.notNull(dest);
		this.arg = Check.notNull(arg);
	}

	public Op op() {
		return op;
	}

	public Temp dest() {
		return dest;
	}

	public Temp arg() {
		return arg;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
