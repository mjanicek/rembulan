package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class Call extends BodyNode {

	private final MultiVal dest;
	private final Val fn;
	private final VList args;

	public Call(MultiVal dest, Val fn, VList args) {
		this.dest = Check.notNull(dest);
		this.fn = Check.notNull(fn);
		this.args = Check.notNull(args);
	}

	public MultiVal dest() {
		return dest;
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
