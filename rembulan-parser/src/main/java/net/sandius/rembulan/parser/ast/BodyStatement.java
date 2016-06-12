package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public abstract class BodyStatement implements Statement {

	private final SourceInfo src;

	protected BodyStatement(SourceInfo src) {
		this.src = Check.notNull(src);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

}
