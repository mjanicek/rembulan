package net.sandius.rembulan.compiler.ir;

public class RegProvider {

	private int tempIdx;
	private int varIdx;
	private int uvIdx;
	private int labelIdx;

	public RegProvider() {
		this.tempIdx = 0;
		this.varIdx = 0;
		this.uvIdx = 0;
		this.labelIdx = 0;
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

	public Label newLabel() {
		return new Label(labelIdx++);
	}

}
