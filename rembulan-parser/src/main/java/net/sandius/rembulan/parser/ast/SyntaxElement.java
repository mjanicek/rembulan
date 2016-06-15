package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public abstract class SyntaxElement {

	private final Attributes attr;

	protected SyntaxElement(Attributes attr) {
		this.attr = Check.notNull(attr);
	}

	public SourceInfo sourceInfo() {
		return attr.get(SourceInfo.class);
	}

	public Attributes attributes() {
		return attr;
	}

}
