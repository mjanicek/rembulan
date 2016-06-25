package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabStackAppend extends BodyNode {

	private final Val obj;

	public TabStackAppend(Val obj) {
		this.obj = Check.notNull(obj);
	}

	public Val obj() {
		return obj;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
