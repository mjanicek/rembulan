package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class VarMapping {

	private final List<Variable> vars;

	public VarMapping(List<Variable> vars) {
		this.vars = Check.notNull(vars);
	}

	public List<Variable> vars() {
		return vars;
	}

}
