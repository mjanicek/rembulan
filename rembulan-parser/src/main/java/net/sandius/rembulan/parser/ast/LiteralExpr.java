package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class LiteralExpr implements RValueExpr {

	private final Literal value;

	public LiteralExpr(Literal value) {
		this.value = Check.notNull(value);
	}

	public Literal value() {
		return value;
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitLiteral(value);
	}

}
