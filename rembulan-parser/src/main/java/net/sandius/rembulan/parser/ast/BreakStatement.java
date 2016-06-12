package net.sandius.rembulan.parser.ast;

public class BreakStatement extends BodyStatement {

	public BreakStatement(SourceInfo src) {
		super(src);
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visit(this);
	}

}
