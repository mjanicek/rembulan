package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class Ret extends BlockTermNode {

	private final VList args;

	public Ret(VList args) {
		this.args = Check.notNull(args);
	}

	public VList args() {
		return args;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
