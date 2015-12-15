package net.sandius.rembulan.core;

public abstract class Closure extends Function {

	public Closure() {
		super();
	}

	protected Object getUpValue(ObjectStack objectStack, int idx) {
		// FIXME
		return objectStack.get(idx);
	}

}
