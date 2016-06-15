package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class DoStatement extends BodyStatement {

	private final Block block;

	public DoStatement(SourceInfo src, Block block) {
		super(src);
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
			return new DoStatement(sourceInfo(), block);
		}
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public BodyStatement acceptTransformer(Transformer tf) {
		return tf.transform(this);
	}

}
