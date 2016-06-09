package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class TableConstructorExpr implements Expr {

	private final List<FieldInitialiser> fields;

	public TableConstructorExpr(List<FieldInitialiser> fields) {
		this.fields = Check.notNull(fields);
	}

}
