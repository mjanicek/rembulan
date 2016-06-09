package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class DoStatement implements Statement {

	private final Block block;

	public DoStatement(Block block) {
		this.block = Check.notNull(block);
	}

	public Block block() {
		return block;
	}

}
