package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public abstract class Expr implements SyntaxElement {

	private SourceInfo src;

	protected Expr(SourceInfo src) {
		this.src = Check.notNull(src);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

	public abstract Expr acceptTransformer(Transformer tf);

}
