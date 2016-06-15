package net.sandius.rembulan.parser.ast;

public class BreakStatement extends BodyStatement {

	public BreakStatement(SourceInfo src) {
		super(src);
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
