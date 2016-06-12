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

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visit(this);
	}

}
