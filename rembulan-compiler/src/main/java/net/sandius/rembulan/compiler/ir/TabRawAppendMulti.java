package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabRawAppendMulti extends BodyNode {

	private final Val obj;
	private final int firstIdx;
	private final MultiVal src;

	public TabRawAppendMulti(Val obj, int firstIdx, MultiVal src) {
		this.obj = Check.notNull(obj);
		this.firstIdx = Check.positive(firstIdx);
		this.src = Check.notNull(src);
	}

	public Val obj() {
		return obj;
	}

	public int firstIdx() {
		return firstIdx;
	}

	public MultiVal src() {
		return src;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
