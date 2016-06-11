package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.parser.util.Util;
import net.sandius.rembulan.util.Check;

import java.util.List;

public class TableConstructorExpr implements RValueExpr {

	private final List<FieldInitialiser> fields;

	public TableConstructorExpr(List<FieldInitialiser> fields) {
		this.fields = Check.notNull(fields);
	}

	@Override
	public String toString() {
		return "(newtable [" + Util.listToString(fields, ", ") + "])";
	}

}
