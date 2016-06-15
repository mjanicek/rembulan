package net.sandius.rembulan.parser.ast;

public class BreakStatement extends BodyStatement {

	public BreakStatement(Attributes attr) {
		super(attr);
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
