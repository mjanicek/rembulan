package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabSetInt extends BodyNode {

	private final Val obj;
	private final int idx;
	private final Val value;

	public TabSetInt(Val obj, int idx, Val value) {
		this.obj = Check.notNull(obj);
		this.idx = Check.positive(idx);
		this.value = Check.notNull(value);
	}

	public Val obj() {
		return obj;
	}

	public int idx() {
		return idx;
	}

	public Val value() {
		return value;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
