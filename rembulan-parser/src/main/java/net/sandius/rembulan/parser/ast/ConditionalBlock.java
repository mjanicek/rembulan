package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class ConditionalBlock {

	private final Expr condition;
	private final Block block;

	public ConditionalBlock(Expr condition, Block block) {
		this.condition = Check.notNull(condition);
		this.block = Check.notNull(block);
	}

	@Override
	public String toString() {
		return "(cond-block " + condition + " " + block + ")";
	}

	public Expr condition() {
		return condition;
	}

	public Block block() {
		return block;
	}

}
