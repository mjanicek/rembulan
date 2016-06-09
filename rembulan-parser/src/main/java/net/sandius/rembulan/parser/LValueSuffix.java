package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.DerefExpr;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.FieldRef;
import net.sandius.rembulan.parser.ast.LValue;
import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.List;

class LValueSuffix {

	private final List<InvokeOp> invokes;
	private final Expr fieldIndex;

	LValueSuffix(List<InvokeOp> invokes, Expr fieldIndex) {
		this.invokes = Check.notNull(invokes);
		this.fieldIndex = Check.notNull(fieldIndex);
	}

	LValueSuffix(Expr fieldIndex) {
		this(Collections.<InvokeOp>emptyList(), fieldIndex);
	}

	public LValue applyOn(LValue lv) {
		return applyOn(new DerefExpr(lv));
	}

	public LValue applyOn(Expr e) {
		for (InvokeOp inv : invokes) {
			e = inv.on(e);
		}
		return new FieldRef(e, fieldIndex);
	}

}
