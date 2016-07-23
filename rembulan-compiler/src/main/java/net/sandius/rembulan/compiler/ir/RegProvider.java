package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.parser.ast.Name;

public class RegProvider {

	private int valIdx;
	private int phiValIdx;
	private int multiValIdx;

	private int varIdx;

	public RegProvider() {
		this.valIdx = 0;
		this.phiValIdx = 0;
		this.varIdx = 0;
		this.multiValIdx = 0;
	}

	public Val newVal() {
		return new Val(valIdx++);
	}

	public PhiVal newPhiVal() {
		return new PhiVal(phiValIdx++);
	}

	public MultiVal newMultiVal() {
		return new MultiVal(multiValIdx++);
	}

	public Var newVar() {
		return new Var(varIdx++);
	}

	public UpVar newUpVar(Name name) {
		return new UpVar(name);
	}

}
