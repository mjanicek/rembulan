package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class AssignStatement implements Statement {

	private final List<LValueExpr> vars;
	private final List<Expr> exps;

	public AssignStatement(List<LValueExpr> vars, List<Expr> exps) {
		this.vars = Check.notNull(vars);
		this.exps = Check.notNull(exps);
	}

	@Override
	public String toString() {
		return "(local [" + Util.listToString(vars, ", ") + "] [" + Util.listToString(exps, ", ") + "])";
	}

}
