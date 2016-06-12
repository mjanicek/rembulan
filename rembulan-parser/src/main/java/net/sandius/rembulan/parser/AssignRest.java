package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.AssignStatement;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.LValueExpr;
import net.sandius.rembulan.parser.ast.SourceInfo;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AssignRest {

	public final SourceInfo src;

	public final List<LValueExpr> vars;
	public final List<Expr> exprs;

	AssignRest(SourceInfo src, List<LValueExpr> vars, List<Expr> exprs) {
		this.src = Check.notNull(src);
		this.vars = Check.notNull(vars);
		this.exprs = Check.notNull(exprs);
	}

	AssignRest(SourceInfo src, List<Expr> exprs) {
		this(src, Collections.<LValueExpr>emptyList(), exprs);
	}

	AssignStatement prepend(LValueExpr v) {
		Check.notNull(v);
		List<LValueExpr> vs = new ArrayList<>();
		vs.add(v);
		vs.addAll(vars);
		return new AssignStatement(src, Collections.unmodifiableList(vs), exprs);
	}

}
