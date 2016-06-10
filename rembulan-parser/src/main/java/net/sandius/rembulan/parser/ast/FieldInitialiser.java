package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class FieldInitialiser {

	private final Expr keyExpr;  // may be null
	private final Expr valueExpr;

	public FieldInitialiser(Expr keyExpr, Expr valueExpr) {
		this.keyExpr = keyExpr;
		this.valueExpr = Check.notNull(valueExpr);
	}

	@Override
	public String toString() {
		return "(field-init " + (keyExpr != null ? keyExpr : "anon") + " " + valueExpr + ")";
	}

}
