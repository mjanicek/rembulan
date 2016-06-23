package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class JmpIfNil extends BranchNode {

	private final Temp addr;

	public JmpIfNil(Temp addr, Label jmpDest, Label next) {
		super(jmpDest, next);
		this.addr = Check.notNull(addr);
	}

	public Temp addr() {
		return addr;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
