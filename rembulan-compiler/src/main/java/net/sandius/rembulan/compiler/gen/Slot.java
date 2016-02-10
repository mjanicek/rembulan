package net.sandius.rembulan.compiler.gen;

import java.util.Objects;

public class Slot {

	protected final Origin origin;
	protected final Type type;

	public Slot(Origin origin, Type type) {
		this.origin = Objects.requireNonNull(origin);
		this.type = Objects.requireNonNull(type);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Slot that = (Slot) o;

		return origin.equals(that.origin) && type.equals(that.type);
	}

	@Override
	public int hashCode() {
		int result = origin.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return origin.toString() + ":" + type.toString();
	}

	public Origin origin() {
		return origin;
	}

	public Type type() {
		return type;
	}

}
