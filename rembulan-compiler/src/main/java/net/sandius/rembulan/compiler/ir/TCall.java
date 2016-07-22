package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.Collections;

public class TCall extends BlockTermNode {

	private final Val target;
	private final VList args;

	public TCall(Val target, VList args) {
		this.target = Check.notNull(target);
		this.args = Check.notNull(args);
	}

	public Val target() {
		return target;
	}

	public VList args() {
		return args;
	}

	@Override
	public Iterable<Label> nextLabels() {
		return Collections.emptyList();
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
