package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class ToNumber extends BodyNode {

	private final Val dest;
	private final Val src;

	public ToNumber(Val dest, Val src) {
		this.dest = Check.notNull(dest);
		this.src = Check.notNull(src);
	}

	public Val dest() {
		return dest;
	}

	public Val src() {
		return src;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
