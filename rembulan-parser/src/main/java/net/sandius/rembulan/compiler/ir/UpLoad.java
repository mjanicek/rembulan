package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class UpLoad extends IRNode {

	private final Temp dest;
	private final UpVar uv;

	public UpLoad(Temp dest, UpVar uv) {
		this.dest = Check.notNull(dest);
		this.uv = Check.notNull(uv);
	}

	public Temp dest() {
		return dest;
	}

	public UpVar upval() {
		return uv;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
