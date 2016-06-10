package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class WhileStatement implements Statement {

	private final Expr condition;
	private final Block block;

	public WhileStatement(Expr condition, Block block) {
		this.condition = Check.notNull(condition);
		this.block = Check.notNull(block);
	}

	public Expr condition() {
		return condition;
	}

	public Block block() {
		return block;
	}

	@Override
	public String toString() {
		return "(while " + condition + " " + block + ")";
	}

}
