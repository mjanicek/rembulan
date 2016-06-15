package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class ConditionalBlock {

	private final Expr condition;
	private final Block block;

	public ConditionalBlock(Expr condition, Block block) {
		this.condition = Check.notNull(condition);
		this.block = Check.notNull(block);
	}

	public Expr condition() {
		return condition;
	}

	public Block block() {
		return block;
	}

	public ConditionalBlock update(Expr condition, Block block) {
		if (this.condition.equals(condition) && this.block.equals(block)) {
			return this;
		}
		else {
			return new ConditionalBlock(condition, block);
		}
	}

}
