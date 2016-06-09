package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.Operator;
import net.sandius.rembulan.util.Check;

class ExprBuilder {

	private Expr expr;

	ExprBuilder() {
		this.expr = null;
	}

	public void add(Operator.Unary op) {
		Check.notNull(op);
		throw new UnsupportedOperationException();  // TODO
	}

	public void add(Operator.Binary op) {
		Check.notNull(op);
		throw new UnsupportedOperationException();  // TODO
	}

	public void add(Expr expr) {
		Check.notNull(expr);

		// FIXME

		if (this.expr != null) {
			throw new UnsupportedOperationException();
		}
		else {
			this.expr = expr;
		}
	}

	public Expr build() {

		// FIXME

		if (expr != null) {
			return expr;
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

}
