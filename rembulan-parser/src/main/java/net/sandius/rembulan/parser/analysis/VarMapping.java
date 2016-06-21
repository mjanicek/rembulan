package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.List;

public class VarMapping {

	private final List<Variable> vars;

	public VarMapping(List<Variable> vars) {
		Check.notNull(vars);
		if (vars.isEmpty()) {
			throw new IllegalArgumentException("variable list is empty");
		}
		this.vars = vars;
	}

	public VarMapping(Variable v) {
		this(Collections.singletonList(Check.notNull(v)));
	}

	public List<Variable> vars() {
		return vars;
	}

	public Variable get() {
		return vars.get(0);
	}

}
