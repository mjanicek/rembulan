package net.sandius.rembulan.parser.ast;

public abstract class BodyStatement extends Statement {

	protected BodyStatement(Attributes attr) {
		super(attr);
	}

	public abstract BodyStatement accept(Transformer tf);

}
