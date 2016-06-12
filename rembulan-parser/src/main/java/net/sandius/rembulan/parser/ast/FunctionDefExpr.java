package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class FunctionDefExpr implements RValueExpr {

	private final SourceInfo src;
	private final FunctionLiteral body;

	public FunctionDefExpr(SourceInfo src, FunctionLiteral body) {
		this.src = Check.notNull(src);
		this.body = Check.notNull(body);
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitFunctionDef(body);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

}
