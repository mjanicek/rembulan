package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class AssignStatement extends BodyStatement {

	private final List<LValueExpr> vars;
	private final List<Expr> exprs;

	public AssignStatement(SourceInfo src, List<LValueExpr> vars, List<Expr> exprs) {
		super(src);
		this.vars = Check.notNull(vars);
		this.exprs = Check.notNull(exprs);
	}

	public List<LValueExpr> vars() {
		return vars;
	}

	public List<Expr> exprs() {
		return exprs;
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visit(this);
	}

}
