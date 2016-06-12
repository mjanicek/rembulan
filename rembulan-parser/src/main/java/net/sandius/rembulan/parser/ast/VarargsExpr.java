package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class VarargsExpr implements RValueExpr {

	private final SourceInfo src;

	public VarargsExpr(SourceInfo src) {
		this.src = Check.notNull(src);
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitVarargs();
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

}
