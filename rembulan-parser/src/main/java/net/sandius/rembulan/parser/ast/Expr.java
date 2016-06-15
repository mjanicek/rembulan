package net.sandius.rembulan.parser.ast;

public abstract class Expr extends SyntaxElement {

	protected Expr(Attributes attr) {
		super(attr);
	}

	public abstract Expr accept(Transformer tf);

}
