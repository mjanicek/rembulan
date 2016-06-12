package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class IndexExpr implements LValueExpr, RValueExpr {

	private final SourceInfo src;
	private final Expr object;
	private final Expr key;

	public IndexExpr(SourceInfo src, Expr object, Expr key) {
		this.src = Check.notNull(src);
		this.object = Check.notNull(object);
		this.key = Check.notNull(key);
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitIndex(object, key);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

}
