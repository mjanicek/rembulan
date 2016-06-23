package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class Call extends BodyNode {

	private final Val fn;
	private final VList args;

	public Call(Val fn, VList args) {
		this.fn = Check.notNull(fn);
		this.args = Check.notNull(args);
	}

	public Val fn() {
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
