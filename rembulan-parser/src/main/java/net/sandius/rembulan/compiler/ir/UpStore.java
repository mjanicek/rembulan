package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class UpStore extends BodyNode {

	private final UpVar uv;
	private final Temp src;

	public UpStore(UpVar uv, Temp src) {
		this.uv = Check.notNull(uv);
		this.src = Check.notNull(src);
	}

	public UpVar upval() {
		return uv;
	}

	public Temp src() {
		return src;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
