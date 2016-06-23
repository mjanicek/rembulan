package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public abstract class BranchNode extends BlockTermNode implements JmpNode {

	private final Label branch;
	private final Label next;

	protected BranchNode(Label branch, Label next) {
		this.branch = Check.notNull(branch);
		this.next = Check.notNull(next);
	}

	@Override
	public Label jmpDest() {
		return branch;
	}

	public Label next() {
		return next;
	}

}
