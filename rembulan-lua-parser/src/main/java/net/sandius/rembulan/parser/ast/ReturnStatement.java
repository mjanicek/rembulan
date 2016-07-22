package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class ReturnStatement extends Statement {

	private final List<Expr> exprs;

	public ReturnStatement(Attributes attr, List<Expr> exprs) {
		super(attr);
		this.exprs = Check.notNull(exprs);
	}

	public List<Expr> exprs() {
		return exprs;
	}

	public ReturnStatement update(List<Expr> exprs) {
		if (this.exprs.equals(exprs)) {
			return this;
		}
		else {
			return new ReturnStatement(attributes(), exprs);
		}
	}

	public ReturnStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
