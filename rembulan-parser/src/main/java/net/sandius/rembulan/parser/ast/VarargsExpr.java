package net.sandius.rembulan.parser.ast;

public class VarargsExpr extends Expr {

	public VarargsExpr(SourceInfo src) {
		super(src);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Expr acceptTransformer(ExprTransformer tf) {
		return tf.transform(this);
	}

}
