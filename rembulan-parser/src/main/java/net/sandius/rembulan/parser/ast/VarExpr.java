package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class VarExpr implements LValueExpr, RValueExpr {

	private final SourceInfo src;
	private final Name name;

	public VarExpr(SourceInfo src, Name name) {
		this.src = Check.notNull(src);
		this.name = Check.notNull(name);
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitVar(name);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

}
