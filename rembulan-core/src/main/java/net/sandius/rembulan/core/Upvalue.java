package net.sandius.rembulan.core;

public class Upvalue {

	private Object value;

	public Upvalue(Object value) {
		this.value = value;
	}

	public Upvalue() {
		this(null);
	}

	public Object get() {
		return value;
	}

	public void set(Object value) {
		this.value = value;
	}

}
