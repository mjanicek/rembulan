package net.sandius.rembulan.parser.ast;

public abstract class BodyStatement extends Statement {

	protected BodyStatement(SourceInfo src) {
		super(src);
	}

	public abstract BodyStatement acceptTransformer(Transformer tf);

}
