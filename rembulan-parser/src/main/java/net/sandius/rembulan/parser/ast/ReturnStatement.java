package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class ReturnStatement extends Statement {

	private final List<Expr> exprs;

	public ReturnStatement(SourceInfo src, List<Expr> exprs) {
		super(src);
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
			return new ReturnStatement(sourceInfo(), exprs);
		}
	}

	public ReturnStatement acceptTransformer(Transformer tf) {
		return tf.transform(this);
	}

}
