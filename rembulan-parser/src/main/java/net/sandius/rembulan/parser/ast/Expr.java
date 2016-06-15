package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public abstract class Expr implements SyntaxElement {

	private final SourceInfo src;
	private final Attributes attr;

	protected Expr(SourceInfo src, Attributes attr) {
		this.src = Check.notNull(src);
		this.attr = Check.notNull(attr);
	}

	protected Expr(SourceInfo src) {
		this(src, Attributes.empty());
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

	public Attributes attributes() {
		return attr;
	}

	public abstract Expr accept(Transformer tf);

}
