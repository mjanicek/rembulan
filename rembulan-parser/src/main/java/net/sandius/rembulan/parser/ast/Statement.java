package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public abstract class Statement implements SyntaxElement {

	private final SourceInfo src;
	private final Attributes attr;

	protected Statement(SourceInfo src, Attributes attr) {
		this.src = Check.notNull(src);
		this.attr = Check.notNull(attr);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

	public Attributes attributes() {
		return attr;
	}

}
