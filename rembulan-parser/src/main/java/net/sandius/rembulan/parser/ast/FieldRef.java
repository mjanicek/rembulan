package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class FieldRef implements LValueExpr, RValueExpr {

	private final SourceInfo src;
	private final Expr object;
	private final Expr key;

	public FieldRef(SourceInfo src, Expr object, Expr key) {
		this.src = Check.notNull(src);
		this.object = Check.notNull(object);
		this.key = Check.notNull(key);
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitFieldRef(object, key);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

}
