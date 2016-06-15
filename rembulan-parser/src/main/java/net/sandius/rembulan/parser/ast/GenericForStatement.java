package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class GenericForStatement extends BodyStatement {

	private final List<Name> names;
	private final List<Expr> exprs;
	private final Block block;

	public GenericForStatement(SourceInfo src, List<Name> names, List<Expr> exprs, Block block) {
		super(src);
		this.names = Check.notNull(names);
		this.exprs = Check.notNull(exprs);
		this.block = Check.notNull(block);
	}

	public List<Name> names() {
		return names;
	}

	public List<Expr> exprs() {
		return exprs;
	}

	public Block block() {
		return block;
	}

	public GenericForStatement update(List<Name> names, List<Expr> exprs, Block block) {
		if (this.names.equals(names) && this.exprs.equals(exprs) && this.block.equals(block)) {
			return this;
		}
		else {
			return new GenericForStatement(sourceInfo(), names, exprs, block);
		}
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
