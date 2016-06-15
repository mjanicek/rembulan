package net.sandius.rembulan.parser.ast;

public abstract class Expr extends SyntaxElement {

	protected Expr(SourceInfo src, Attributes attr) {
		super(src, attr);
	}

	public abstract Expr accept(Transformer tf);

}
