package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class Vararg extends BodyNode {

	private final MultiVal dest;

	public Vararg(MultiVal dest) {
		this.dest = Check.notNull(dest);
	}

	public MultiVal dest() {
		return dest;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
