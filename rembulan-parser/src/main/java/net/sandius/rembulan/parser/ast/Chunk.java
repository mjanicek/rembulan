package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class Chunk {

	private final Block block;

	public Chunk(Block block) {
		this.block = Check.notNull(block);
	}

	public Block block() {
		return block;
	}

}
