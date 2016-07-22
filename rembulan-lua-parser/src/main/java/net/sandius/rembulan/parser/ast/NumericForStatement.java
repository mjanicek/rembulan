package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.Objects;

public class NumericForStatement extends BodyStatement {

	private final Name name;
	private final Expr init;
	private final Expr limit;
	private final Expr step;  // may be null
	private final Block block;

	public NumericForStatement(Attributes attr, Name name, Expr init, Expr limit, Expr step, Block block) {
		super(attr);
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
			return new NumericForStatement(attributes(), name, init, limit, step, block);
		}
	}

	public NumericForStatement withAttributes(Attributes attr) {
		if (attributes().equals(attr)) return this;
		else return new NumericForStatement(attr, name, init, limit, step, block);
	}

	public NumericForStatement with(Object o) {
		return this.withAttributes(attributes().with(o));
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
