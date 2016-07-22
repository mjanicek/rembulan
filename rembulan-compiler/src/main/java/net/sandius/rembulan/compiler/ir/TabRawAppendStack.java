package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabRawAppendStack extends BodyNode {

	private final Val obj;
	private final int firstIdx;

	public TabRawAppendStack(Val obj, int firstIdx) {
		this.obj = Check.notNull(obj);
		this.firstIdx = Check.positive(firstIdx);
	}

	public Val obj() {
		return obj;
	}

	public int firstIdx() {
		return firstIdx;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
