package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.LValue;
import net.sandius.rembulan.parser.ast.LiteralExpr;
import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.parser.ast.StringLiteral;
import net.sandius.rembulan.parser.ast.Var;
import net.sandius.rembulan.util.Check;

import java.util.List;

class FunctionName {

	private final Name base;
	private final List<Name> dotted;  // may be empty
	private final Name method;  // may be null

	private FunctionName(Name base, List<Name> dotted, Name method) {
		this.base = Check.notNull(base);
		this.dotted = Check.notNull(dotted);
		this.method = method;
	}

	public static FunctionName of(Name base, List<Name> dotted, Name method) {
		return new FunctionName(base, dotted, method);
	}

	public boolean isMethod() {
		return method != null;
	}

	private static LValueSuffix nameToSuffix(Name n) {
		return new LValueSuffix(new LiteralExpr(StringLiteral.fromName(n)));
	}

	public LValue toLValue() {
		LValue lv = new Var(base);
		for (Name n : dotted) {
			lv = nameToSuffix(n).applyOn(lv);
		}
		if (method != null) {
			lv = nameToSuffix(method).applyOn(lv);
		}
		return lv;
	}

}
