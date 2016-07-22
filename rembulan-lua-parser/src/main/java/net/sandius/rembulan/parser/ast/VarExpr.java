package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class VarExpr extends LValueExpr {

	private final Name name;

	public VarExpr(Attributes attr, Name name) {
		super(attr);
		this.name = Check.notNull(name);
	}

	public Name name() {
		return name;
	}

	public VarExpr withAttributes(Attributes attr) {
		if (attributes().equals(attr)) return this;
		else return new VarExpr(attr, name);
	}

	public VarExpr with(Object o) {
		return this.withAttributes(attributes().with(o));
	}

	@Override
	public LValueExpr accept(Transformer tf) {
		return tf.transform(this);
	}

}
