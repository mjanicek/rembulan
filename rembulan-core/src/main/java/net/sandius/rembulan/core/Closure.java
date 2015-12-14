package net.sandius.rembulan.core;

public abstract class Closure extends Function {

	public Closure() {
		super();
	}

	protected Object getUpValue(int idx) {
		throw new UnsupportedOperationException();
	}

}
