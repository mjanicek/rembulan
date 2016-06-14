package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class LabelStatement extends BodyStatement {

	private final Name labelName;

	public LabelStatement(SourceInfo src, Name labelName) {
		super(src);
		this.labelName = Check.notNull(labelName);
	}

	public Name labelName() {
		return labelName;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
