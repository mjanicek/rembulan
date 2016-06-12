package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class IndexExpr extends LValueExpr {

	private final Expr object;
	private final Expr key;

	public IndexExpr(SourceInfo src, Expr object, Expr key) {
		super(src);
		this.object = Check.notNull(object);
		this.key = Check.notNull(key);
	}

	public Expr object() {
		return object;
	}

	public Expr key() {
		return key;
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visit(this);
	}

}
