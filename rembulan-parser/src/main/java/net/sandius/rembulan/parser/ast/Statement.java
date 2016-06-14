package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public abstract class Statement implements SyntaxElement, Visitable {

	private final SourceInfo src;

	protected Statement(SourceInfo src) {
		this.src = Check.notNull(src);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

}
