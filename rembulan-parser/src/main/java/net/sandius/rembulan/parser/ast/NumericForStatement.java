package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.Objects;

public class NumericForStatement extends BodyStatement {

	private final Name name;
	private final Expr init;
	private final Expr limit;
	private final Expr step;  // may be null
	private final Block block;

	public NumericForStatement(SourceInfo src, Name name, Expr init, Expr limit, Expr step, Block block) {
		super(src);
		this.name = Check.notNull(name);
		this.init = Check.notNull(init);
		this.limit = Check.notNull(limit);
		this.step = step;
		this.block = Check.notNull(block);
	}

	public Name name() {
		return name;
	}

	public Expr init() {
		return init;
	}

	public Expr limit() {
		return limit;
	}

	public Expr step() {
		return step;
	}

	public Block block() {
		return block;
	}

	public NumericForStatement update(Name name, Expr init, Expr limit, Expr step, Block block) {
		if (this.name.equals(name) && this.init.equals(init) && this.limit.equals(limit)
				&& Objects.equals(this.step, step) && this.block.equals(block)) {
			return this;
		}
		else {
			return new NumericForStatement(sourceInfo(), name, init, limit, step, block);
		}
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
