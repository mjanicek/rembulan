package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class RepeatUntilStatement extends BodyStatement {

	private final Expr condition;
	private final Block block;

	public RepeatUntilStatement(Attributes attr, Expr condition, Block block) {
		super(attr);
		this.condition = Check.notNull(condition);
		this.block = Check.notNull(block);
	}

	public Expr condition() {
		return condition;
	}

	public Block block() {
		return block;
	}

	public RepeatUntilStatement update(Expr condition, Block block) {
		if (this.condition.equals(condition) && this.block.equals(block)) {
			return this;
		}
		else {
			return new RepeatUntilStatement(attributes(), condition, block);
		}
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
