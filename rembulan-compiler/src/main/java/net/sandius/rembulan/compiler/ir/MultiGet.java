package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class MultiGet extends BodyNode {

	private final Val dest;
	private final MultiVal src;
	private final int idx;

	public MultiGet(Val dest, MultiVal src, int idx) {
		this.dest = Check.notNull(dest);
		this.src = Check.notNull(src);
		this.idx = idx;
	}

	public Val dest() {
		return dest;
	}

	public MultiVal src() {
		return src;
	}

	public int idx() {
		return idx;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
