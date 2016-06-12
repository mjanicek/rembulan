package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class LiteralExpr implements RValueExpr {

	private final SourceInfo src;
	private final Literal value;

	public LiteralExpr(SourceInfo src, Literal value) {
		this.src = Check.notNull(src);
		this.value = Check.notNull(value);
	}

	public Literal value() {
		return value;
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitLiteral(value);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

}
