package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class LabelStatement implements Statement {

	private final Name labelName;

	public LabelStatement(Name labelName) {
		this.labelName = Check.notNull(labelName);
	}

	public Name labelName() {
		return labelName;
	}

	@Override
	public String toString() {
		return "(label-def " + labelName + ")";
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitLabel(labelName);
	}

}
