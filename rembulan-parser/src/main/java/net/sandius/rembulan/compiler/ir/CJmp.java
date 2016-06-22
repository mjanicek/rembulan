package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class CJmp extends BodyNode implements JmpNode {

	private final Temp addr;
	private final boolean expected;

	private final Label jmpDest;

	public CJmp(Temp addr, boolean expected, Label jmpDest) {
		this.addr = Check.notNull(addr);
		this.expected = expected;
		this.jmpDest = Check.notNull(jmpDest);
	}

	public Temp addr() {
		return addr;
	}

	public boolean expected() {
		return expected;
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
