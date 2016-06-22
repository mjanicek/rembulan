package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class ToNumber extends BodyNode {

	private final Temp dest;
	private final Temp src;

	public ToNumber(Temp dest, Temp src) {
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
