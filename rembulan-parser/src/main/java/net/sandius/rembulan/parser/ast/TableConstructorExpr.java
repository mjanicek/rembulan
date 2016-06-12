package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class TableConstructorExpr implements RValueExpr {

	private final SourceInfo src;
	private final List<FieldInitialiser> fields;

	public TableConstructorExpr(SourceInfo src, List<FieldInitialiser> fields) {
		this.src = Check.notNull(src);
		this.fields = Check.notNull(fields);
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitTableConstructor(fields);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

	public static class FieldInitialiser {

		private final Expr keyExpr;  // may be null
		private final Expr valueExpr;

		public FieldInitialiser(Expr keyExpr, Expr valueExpr) {
			this.keyExpr = keyExpr;
			this.valueExpr = Check.notNull(valueExpr);
		}

		public Expr key() {
			return keyExpr;
		}

		public Expr value() {
			return valueExpr;
		}

	}

}
