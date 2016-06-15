package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class VarExpr extends LValueExpr {

	private final Name name;

	public VarExpr(SourceInfo src, Attributes attr, Name name) {
		super(src, attr);
		this.name = Check.notNull(name);
	}

	public Name name() {
		return name;
	}

	@Override
	public LValueExpr accept(Transformer tf) {
		return tf.transform(this);
	}

}
