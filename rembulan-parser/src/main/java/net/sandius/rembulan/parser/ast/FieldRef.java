package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class FieldRef implements LValueExpr, RValueExpr {

	private final Expr object;
	private final Expr key;

	public FieldRef(Expr object, Expr key) {
		this.object = Check.notNull(object);
		this.key = Check.notNull(key);
	}

	@Override
	public String toString() {
		return "(field-ref " + object + " "  + key + ")";
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitFieldRef(object, key);
	}

}
