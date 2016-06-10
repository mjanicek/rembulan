package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.AssignStatement;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.LValue;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AssignRest {

	public final List<LValue> vars;
	public final List<Expr> exprs;

	AssignRest(List<LValue> vars, List<Expr> exprs) {
		this.vars = Check.notNull(vars);
		this.exprs = Check.notNull(exprs);
	}

	AssignRest(List<Expr> exprs) {
		this(Collections.<LValue>emptyList(), exprs);
	}

	AssignStatement prepend(LValue v) {
		Check.notNull(v);
		List<LValue> vs = new ArrayList<>();
		vs.add(v);
		vs.addAll(vars);
		return new AssignStatement(Collections.unmodifiableList(vs), exprs);
	}

}
