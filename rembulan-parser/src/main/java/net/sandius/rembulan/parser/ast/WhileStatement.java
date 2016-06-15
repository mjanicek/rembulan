package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class WhileStatement extends BodyStatement {

	private final Expr condition;
	private final Block block;

	public WhileStatement(SourceInfo src, Expr condition, Block block) {
		super(src);
		this.condition = Check.notNull(condition);
		this.block = Check.notNull(block);
	}

	public Expr condition() {
		return condition;
	}

	public Block block() {
		return block;
	}

	public WhileStatement update(Expr condition, Block block) {
		if (this.condition.equals(condition) && this.block.equals(block)) {
			return this;
		}
		else {
			return new WhileStatement(sourceInfo(), condition, block);
		}
	}

	@Override
	public BodyStatement acceptTransformer(Transformer tf) {
		return tf.transform(this);
	}

}
