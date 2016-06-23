package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.Collections;

public class ToNext extends BlockTermNode {

	private final Label label;

	public ToNext(Label label) {
		this.label = Check.notNull(label);
	}

	public Label label() {
		return label;
	}

	@Override
	public Iterable<Label> nextLabels() {
		return Collections.singletonList(label());
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
