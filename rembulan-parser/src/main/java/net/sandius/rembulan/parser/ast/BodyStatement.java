package net.sandius.rembulan.parser.ast;

public abstract class BodyStatement extends Statement {

	protected BodyStatement(SourceInfo src, Attributes attr) {
		super(src, attr);
	}

	public abstract BodyStatement accept(Transformer tf);

}
