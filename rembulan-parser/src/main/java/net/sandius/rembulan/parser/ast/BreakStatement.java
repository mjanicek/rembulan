package net.sandius.rembulan.parser.ast;

public class BreakStatement implements Statement {

	@Override
	public String toString() {
		return "(break)";
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitBreak();
	}

}
