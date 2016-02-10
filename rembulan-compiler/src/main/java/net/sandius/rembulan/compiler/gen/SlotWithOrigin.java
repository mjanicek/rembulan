package net.sandius.rembulan.compiler.gen;

import java.util.Objects;

public class SlotWithOrigin {

	protected final ValueOrigin origin;
	protected final SlotType type;

	public SlotWithOrigin(ValueOrigin origin, SlotType type) {
		this.origin = Objects.requireNonNull(origin);
		this.type = Objects.requireNonNull(type);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SlotWithOrigin that = (SlotWithOrigin) o;

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

	public ValueOrigin origin() {
		return origin;
	}

	public SlotType type() {
		return type;
	}

}
