package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabStackAppend extends BodyNode {

	private final Val dest;

	public TabStackAppend(Val dest) {
		this.dest = Check.notNull(dest);
	}

	public Val dest() {
		return dest;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
