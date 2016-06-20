package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class Jmp extends IRNode {

	private final Label target;

	public Jmp(Label target) {
		this.target = Check.notNull(target);
	}

	public Label target() {
		return target;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
