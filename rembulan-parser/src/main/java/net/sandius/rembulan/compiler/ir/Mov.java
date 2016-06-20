package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class Mov extends IRNode {

	private final Temp dest;
	private final Temp src;

	public Mov(Temp dest, Temp src) {
		this.dest = Check.notNull(dest);
		this.src = Check.notNull(src);
	}

	public Temp dest() {
		return dest;
	}

	public Temp src() {
		return src;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
