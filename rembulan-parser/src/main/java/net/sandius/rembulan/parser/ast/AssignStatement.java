package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class AssignStatement extends BodyStatement {

	private final List<LValueExpr> vars;
	private final List<Expr> exprs;

	public AssignStatement(List<LValueExpr> vars, List<Expr> exprs) {
		this.vars = Check.notNull(vars);
		this.exprs = Check.notNull(exprs);
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitAssignment(vars, exprs);
	}

}
