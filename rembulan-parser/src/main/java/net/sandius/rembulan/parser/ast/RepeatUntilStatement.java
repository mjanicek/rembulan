package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class RepeatUntilStatement extends BodyStatement {

	private final Expr condition;
	private final Block block;

	public RepeatUntilStatement(SourceInfo src, Expr condition, Block block) {
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

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
