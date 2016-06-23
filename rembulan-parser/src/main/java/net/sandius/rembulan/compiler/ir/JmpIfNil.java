package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class JmpIfNil extends BranchNode implements JmpNode {

	private final Temp addr;
	private final Label jmpDest;

	public JmpIfNil(Temp addr, Label jmpDest) {
		this.addr = Check.notNull(addr);
		this.jmpDest = Check.notNull(jmpDest);
	}

	public Temp addr() {
		return addr;
	}

	@Override
	public Label jmpDest() {
		return jmpDest;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
