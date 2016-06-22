package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class Jmp extends IRNode implements JmpNode, BlockTermNode {

	private final Label jmpDest;

	public Jmp(Label jmpDest) {
		this.jmpDest = Check.notNull(jmpDest);
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
