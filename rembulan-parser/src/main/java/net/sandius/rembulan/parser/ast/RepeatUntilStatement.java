package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class RepeatUntilStatement implements Statement {

	private final Expr condition;
	private final Block block;

	public RepeatUntilStatement(Expr condition, Block block) {
		this.condition = Check.notNull(condition);
		this.block = Check.notNull(block);
	}

	public Expr exp() {
		return condition;
	}

	public Block block() {
		return block;
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitRepeatUntil(condition, block);
	}

}
