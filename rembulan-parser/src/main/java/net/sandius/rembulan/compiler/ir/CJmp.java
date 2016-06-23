package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class CJmp extends BranchNode {

	private final Temp addr;
	private final boolean expected;

	public CJmp(Temp addr, boolean expected, Label jmpDest, Label next) {
		super(jmpDest, next);
		this.addr = Check.notNull(addr);
		this.expected = expected;
	}

	public Temp addr() {
		return addr;
	}

	public boolean expected() {
		return expected;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
