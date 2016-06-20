package net.sandius.rembulan.parser.ast;

public class VarargsExpr extends MultiExpr {

	public VarargsExpr(Attributes attr) {
		super(attr);
	}

	@Override
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

}
