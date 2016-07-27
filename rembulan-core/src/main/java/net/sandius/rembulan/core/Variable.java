package net.sandius.rembulan.core;

public class Variable {

	private Object value;

	public Variable(Object initialValue) {
		this.value = initialValue;
	}

	public Object get() {
		return value;
	}

	public void set(Object value) {
		this.value = value;
	}

}
