package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class VarExpr extends LValueExpr {

	private final Name name;

	public VarExpr(SourceInfo src, Name name) {
		super(src);
		this.name = Check.notNull(name);
	}

	public Name name() {
		return name;
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visit(this);
	}

}
