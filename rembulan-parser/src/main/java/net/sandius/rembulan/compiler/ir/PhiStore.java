package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class PhiStore extends BodyNode {

	private final PhiVal dest;
	private final Val src;

	public PhiStore(PhiVal dest, Val src) {
		this.dest = Check.notNull(dest);
		this.src = Check.notNull(src);
	}

	public PhiVal dest() {
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
