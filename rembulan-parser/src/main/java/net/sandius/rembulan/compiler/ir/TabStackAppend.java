package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabStackAppend extends BodyNode {

	private final Temp dest;

	public TabStackAppend(Temp dest) {
		this.dest = Check.notNull(dest);
	}

	public Temp dest() {
		return dest;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
