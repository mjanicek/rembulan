package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class UpStore extends IRNode {

	private final UpVar uv;

	public UpStore(UpVar uv) {
		this.uv = Check.notNull(uv);
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

	public UpVar upval() {
		return uv;
	}

}
