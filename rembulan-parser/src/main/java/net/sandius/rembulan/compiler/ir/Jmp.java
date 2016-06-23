package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.Collections;

public class Jmp extends BlockTermNode implements JmpNode {

	private final Label jmpDest;

	public Jmp(Label jmpDest) {
		this.jmpDest = Check.notNull(jmpDest);
	}

	@Override
	public Label jmpDest() {
		return jmpDest;
	}

	@Override
	public Iterable<Label> nextLabels() {
		return Collections.singletonList(jmpDest());
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
