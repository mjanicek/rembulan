package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class LiteralExpr extends Expr {

	private final Literal value;

	public LiteralExpr(SourceInfo src, Literal value) {
		super(src);
		this.value = Check.notNull(value);
	}

	public Literal value() {
		return value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
