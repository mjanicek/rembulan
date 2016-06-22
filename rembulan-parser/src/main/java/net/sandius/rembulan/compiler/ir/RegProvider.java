package net.sandius.rembulan.compiler.ir;

public class RegProvider {

	private int tempIdx;
	private int varIdx;
	private int uvIdx;

	public RegProvider() {
		this.tempIdx = 0;
		this.varIdx = 0;
		this.uvIdx = 0;
	}

	public Temp newTemp() {
		return new Temp(tempIdx++);
	}

	public Var newVar() {
		return new Var(varIdx++);
	}

	public UpVar newUpVar() {
		return new UpVar(uvIdx++);
	}

}
