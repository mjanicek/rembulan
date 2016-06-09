package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class GotoStatement implements Statement {

	private final Name labelName;

	public GotoStatement(Name labelName) {
		this.labelName = Check.notNull(labelName);
	}

	public Name labelName() {
		return labelName;
	}

}
