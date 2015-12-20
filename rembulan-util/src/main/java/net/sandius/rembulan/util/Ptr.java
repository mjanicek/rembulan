package net.sandius.rembulan.util;

public class Ptr<T> {

	private T value;

	public Ptr(T value) {
		this.value = value;
	}

	public Ptr() {
		this(null);
	}

	public boolean isNull() {
		return value == null;
	}

	public T get() {
		return value;
	}

	public T getAndClear() {
		T v = value;
		value = null;
		return v;
	}

	public void set(T value) {
		this.value = value;
	}

	public void clear() {
		value = null;
	}

}
