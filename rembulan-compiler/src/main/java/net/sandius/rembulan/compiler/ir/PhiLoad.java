package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class PhiLoad extends BodyNode {

	private final Val dest;
	private final PhiVal src;

	public PhiLoad(Val dest, PhiVal src) {
		this.dest = Check.notNull(dest);
		this.src = Check.notNull(src);
	}

	public Val dest() {
		return dest;
	}

	public PhiVal src() {
		return src;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
