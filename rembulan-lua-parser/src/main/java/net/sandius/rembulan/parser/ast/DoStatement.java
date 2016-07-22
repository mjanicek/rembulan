package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class DoStatement extends BodyStatement {

	private final Block block;

	public DoStatement(Attributes attr, Block block) {
		super(attr);
		this.block = Check.notNull(block);
	}

	public Block block() {
		return block;
	}

	public DoStatement update(Block block) {
		if (this.block.equals(block)) {
			return this;
		}
		else {
			return new DoStatement(attributes(), block);
		}
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
