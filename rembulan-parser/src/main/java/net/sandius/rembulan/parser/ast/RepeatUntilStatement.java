package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class RepeatUntilStatement extends BodyStatement {

	private final Expr condition;
	private final Block block;

	public RepeatUntilStatement(SourceInfo src, Attributes attr, Expr condition, Block block) {
		super(src, attr);
		this.condition = Check.notNull(condition);
		this.block = Check.notNull(block);
	}

	public RepeatUntilStatement(SourceInfo src, Expr condition, Block block) {
		this(src, Attributes.empty(), condition, block);
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
			return new RepeatUntilStatement(sourceInfo(), attributes(), condition, block);
		}
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
