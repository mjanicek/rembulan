package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class DerefExpr implements Expr {

	private final LValue lv;

	public DerefExpr(LValue lv) {
		this.lv = Check.notNull(lv);
	}

}
