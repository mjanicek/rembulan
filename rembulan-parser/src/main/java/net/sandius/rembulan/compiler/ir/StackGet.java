package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class StackGet extends BodyNode {

	private final Val dest;
	private final int idx;

	public StackGet(Val dest, int idx) {
		this.dest = Check.notNull(dest);
		this.idx = idx;
	}

	public Val dest() {
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
