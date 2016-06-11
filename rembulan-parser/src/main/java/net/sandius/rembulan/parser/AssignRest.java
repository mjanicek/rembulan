package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.AssignStatement;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.LValueExpr;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AssignRest {

	public final List<LValueExpr> vars;
	public final List<Expr> exprs;

	AssignRest(List<LValueExpr> vars, List<Expr> exprs) {
		this.vars = Check.notNull(vars);
		this.exprs = Check.notNull(exprs);
	}

	AssignRest(List<Expr> exprs) {
		this(Collections.<LValueExpr>emptyList(), exprs);
	}

	AssignStatement prepend(LValueExpr v) {
		Check.notNull(v);
		List<LValueExpr> vs = new ArrayList<>();
		vs.add(v);
		vs.addAll(vars);
		return new AssignStatement(Collections.unmodifiableList(vs), exprs);
	}

}
