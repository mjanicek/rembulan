package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class StackGet extends IRNode {

	private final Temp dest;
	private final int idx;

	public StackGet(Temp dest, int idx) {
		this.dest = Check.notNull(dest);
		this.idx = idx;
	}

	public Temp dest() {
		return dest;
	}

	public int idx() {
		return idx;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
