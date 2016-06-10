package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class DerefExpr implements Expr, LValue {

	private final LValue lv;

	public DerefExpr(LValue lv) {
		this.lv = Check.notNull(lv);
	}

	@Override
	public String toString() {
		return "(deref " + lv + ")";
	}

}
