package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class ToNext extends IRNode implements BlockTermNode {

	private final Label label;

	public ToNext(Label label) {
		this.label = Check.notNull(label);
	}

	public Label label() {
		return label;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
