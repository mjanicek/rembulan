package net.sandius.rembulan.parser.ast;

public class BreakStatement extends BodyStatement {

	public BreakStatement(SourceInfo src, Attributes attr) {
		super(src, attr);
	}

	public BreakStatement(SourceInfo src) {
		this(src, Attributes.empty());
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
