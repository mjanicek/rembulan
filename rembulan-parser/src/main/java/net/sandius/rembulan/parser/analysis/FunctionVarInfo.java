package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class FunctionVarInfo {

	private final List<Variable> params;
	private final List<Variable> locals;
	private final List<Variable.Ref> upvalues;
	private final boolean vararg;

	public FunctionVarInfo(List<Variable> params, List<Variable> locals, List<Variable.Ref> upvalues, boolean vararg) {
		this.params = Check.notNull(params);
		this.locals = Check.notNull(locals);
		this.upvalues = Check.notNull(upvalues);
		this.vararg = vararg;
	}

	public List<Variable> params() {
		return params;
	}

	public List<Variable> locals() {
		return locals;
	}

	public List<Variable.Ref> upvalues() {
		return upvalues;
	}

	public boolean isVararg() {
		return vararg;
	}

}
