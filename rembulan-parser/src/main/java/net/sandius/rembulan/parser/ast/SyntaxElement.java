package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public abstract class SyntaxElement {

	private final SourceInfo src;
	private final Attributes attr;

	protected SyntaxElement(SourceInfo src, Attributes attr) {
		this.src = Check.notNull(src);
		this.attr = Check.notNull(attr);
	}

	public SourceInfo sourceInfo() {
		return src;
	}

	public Attributes attributes() {
		return attr;
	}

}
