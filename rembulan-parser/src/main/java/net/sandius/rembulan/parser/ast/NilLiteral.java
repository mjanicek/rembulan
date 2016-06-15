package net.sandius.rembulan.parser.ast;

public class NilLiteral implements Literal {

	public static final NilLiteral INSTANCE = new NilLiteral();

	private NilLiteral() {
	}

	@Override
	public Literal acceptTransformer(Transformer tf) {
		return tf.transform(this);
	}

}
