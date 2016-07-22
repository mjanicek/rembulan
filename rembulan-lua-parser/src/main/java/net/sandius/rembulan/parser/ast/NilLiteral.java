package net.sandius.rembulan.parser.ast;

public class NilLiteral extends Literal {

	public static final NilLiteral INSTANCE = new NilLiteral();

	private NilLiteral() {
	}

	@Override
	public Literal accept(Transformer tf) {
		return tf.transform(this);
	}

}
