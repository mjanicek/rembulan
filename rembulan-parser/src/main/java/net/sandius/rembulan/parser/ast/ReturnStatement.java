package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.parser.util.Util;
import net.sandius.rembulan.util.Check;

import java.util.List;

public class ReturnStatement implements StatementVisitable {

	private final List<Expr> exprs;

	public ReturnStatement(List<Expr> exprs) {
		this.exprs = Check.notNull(exprs);
	}

	@Override
	public String toString() {
		return "(ret [" + Util.listToString(exprs, ", ") + "])";
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitReturn(exprs);
	}

}
