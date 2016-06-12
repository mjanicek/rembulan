package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class GotoStatement extends BodyStatement {

	private final Name labelName;

	public GotoStatement(SourceInfo src, Name labelName) {
		super(src);
		this.labelName = Check.notNull(labelName);
	}

	public Name labelName() {
		return labelName;
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitGoto(labelName);
	}

}
