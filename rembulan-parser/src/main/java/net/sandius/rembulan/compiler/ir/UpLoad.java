package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class UpLoad extends BodyNode {

	private final Val dest;
	private final UpVar uv;

	public UpLoad(Val dest, UpVar uv) {
		this.dest = Check.notNull(dest);
		this.uv = Check.notNull(uv);
	}

	public Val dest() {
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
