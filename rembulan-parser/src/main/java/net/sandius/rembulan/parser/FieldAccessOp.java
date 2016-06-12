package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.FieldRef;
import net.sandius.rembulan.parser.ast.SourceInfo;
import net.sandius.rembulan.util.Check;

class FieldAccessOp extends PostfixOp {

	private final SourceInfo src;
	private final Expr keyExpr;

	public FieldAccessOp(SourceInfo src, Expr keyExpr) {
		this.src = Check.notNull(src);
		this.keyExpr = Check.notNull(keyExpr);
	}

	public Expr keyExpr() {
		return keyExpr;
	}

	@Override
	public FieldRef on(Expr exp) {
		return new FieldRef(src, exp, keyExpr);
	}

}
