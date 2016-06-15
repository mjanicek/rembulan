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

	public AssignStatement update(List<LValueExpr> vars, List<Expr> exprs) {
		if (this.vars.equals(vars) && this.exprs.equals(exprs)) {
			return this;
		}
		else {
			return new AssignStatement(sourceInfo(), vars, exprs);
		}
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public BodyStatement acceptTransformer(Transformer tf) {
		return tf.transform(this);
	}

}
