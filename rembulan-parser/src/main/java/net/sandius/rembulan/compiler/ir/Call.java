package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class Call extends IRNode {

	private final Temp fn;
	private final VList args;

	public Call(Temp fn, VList args) {
		this.fn = Check.notNull(fn);
		this.args = Check.notNull(args);
	}

	public Temp fn() {
		return fn;
	}

	public VList args() {
		return args;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
