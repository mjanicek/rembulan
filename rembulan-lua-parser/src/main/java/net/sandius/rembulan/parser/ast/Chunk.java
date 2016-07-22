package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class Chunk {

	private final Attributes attr;
	private final Block block;

	public Chunk(Attributes attr, Block block) {
		this.attr = Check.notNull(attr);
		this.block = Check.notNull(block);
	}

	public Chunk(Block block) {
		this(Attributes.empty(), block);
	}

	public Attributes attributes() {
		return attr;
	}

	public Block block() {
		return block;
	}

	public Chunk update(Block block) {
		if (this.block.equals(block)) {
			return this;
		}
		else {
			return new Chunk(attributes(), block);
		}
	}

	public Chunk with(Object o) {
		Attributes as = attr.with(o);
		if (this.attributes().equals(as)) {
			return this;
		}
		else {
			return new Chunk(as, block);
		}
	}

}
