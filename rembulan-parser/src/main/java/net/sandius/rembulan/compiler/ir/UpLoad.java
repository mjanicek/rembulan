package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class UpLoad extends IRNode {

	private final UpVar uv;

	public UpLoad(UpVar uv) {
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
