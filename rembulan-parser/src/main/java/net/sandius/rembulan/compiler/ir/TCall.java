package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TCall extends BlockTermNode {

	private final Temp target;
	private final VList args;

	public TCall(Temp target, VList args) {
		this.target = Check.notNull(target);
		this.args = Check.notNull(args);
	}

	public Temp target() {
		return target;
	}

	public VList args() {
		return args;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
