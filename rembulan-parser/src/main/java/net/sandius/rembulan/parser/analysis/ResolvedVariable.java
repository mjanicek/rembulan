package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.util.Check;

public class ResolvedVariable {

	private final boolean upvalue;
	private final Variable var;

	private ResolvedVariable(boolean upvalue, Variable var) {
		this.upvalue = upvalue;
		this.var = Check.notNull(var);
	}

	public static ResolvedVariable local(Variable v) {
		return new ResolvedVariable(false, v);
	}

	public static ResolvedVariable upvalue(Variable v) {
		return new ResolvedVariable(true, v);
	}

	public boolean isUpvalue() {
		return upvalue;
	}

	public Variable variable() {
		return var;
	}

	public boolean isGlobal() {
		return var.equals(Variable.GLOBAL);
	}

}
