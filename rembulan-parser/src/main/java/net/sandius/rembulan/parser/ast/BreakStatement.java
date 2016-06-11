package net.sandius.rembulan.parser.ast;

public class BreakStatement extends BodyStatement {

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitBreak();
	}

}
