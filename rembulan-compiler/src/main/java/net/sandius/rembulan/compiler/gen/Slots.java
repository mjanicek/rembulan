package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntSet;
import net.sandius.rembulan.util.ReadOnlyArray;

public class Slots {

	private final ReadOnlyArray<SlotType> types;
	private final IntSet captured;
	private final int varargPosition;  // first index of varargs; if negative, no varargs in slots

	private Slots(ReadOnlyArray<SlotType> types, IntSet captured, int varargPosition) {
		Check.notNull(types);
		Check.notNull(captured);

		int size = types.size();
		if (varargPosition >= 0) Check.inRange(varargPosition, 0, size - 1);

		Check.inRange(captured.size(), 0, size - 1);

		this.types = types;
		this.captured = captured;
		this.varargPosition = varargPosition;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Slots that = (Slots) o;

		// FIXME: vararg position!

		return types.shallowEquals(that.types) && this.captured.equals(that.captured);
	}

	@Override
	public int hashCode() {
		// FIXME: vararg position!

		int result = types.shallowHashCode();
		result = 31 * result + captured.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();

		int numRegularSlots = varargPosition() < 0 ? size() : Math.min(size(), varargPosition());

		for (int i = 0; i < numRegularSlots; i++) {
			SlotType type = getType(i);

			if (isCaptured(i)) {
				bld.append('^');
			}
			bld.append(SlotType.toString(type));
		}

		if (varargPosition() >= 0) {
			bld.append("+");
		}

		return bld.toString();
	}

	public static Slots init(int size) {
		Check.nonNegative(size);

		SlotType[] types = new SlotType[size];

		for (int i = 0; i < size; i++) {
			types[i] = SlotType.NIL;
		}

		return new Slots(ReadOnlyArray.wrap(types), IntSet.empty(), -1);
	}

	public int size() {
		return types.size();
	}

	public ReadOnlyArray<SlotType> types() {
		return types;
	}

	public int varargPosition() {
		return varargPosition;
	}

	public boolean hasVarargs() {
		return varargPosition >= 0;
	}

	public int fixedSize() {
		return varargPosition < 0 ? size() : varargPosition;
	}

	public boolean isValidIndex(int idx) {
		return idx >= 0 && idx < size() && (varargPosition < 0 || idx < varargPosition);
	}

	public boolean isCaptured(int idx) {
		Check.isTrue(isValidIndex(idx));
		return captured.contains(idx);
	}

	public Slots updateState(int idx, boolean cap) {
		Check.isTrue(isValidIndex(idx));

		if (isCaptured(idx) == cap) {
			// no-op
			return this;
		}
		else {
			return new Slots(types, cap ? captured.plus(idx) : captured.minus(idx), varargPosition);
		}
	}

	public Slots capture(int idx) {
		return updateState(idx, true);
	}

	public Slots freshen(int idx) {
		return updateState(idx, false);
	}

	public SlotType getType(int idx) {
		Check.isTrue(isValidIndex(idx));
		return types.get(idx);
	}

	public Slots updateType(int idx, SlotType type) {
		Check.notNull(type);
		Check.isTrue(isValidIndex(idx));

		if (getType(idx).equals(type)) {
			// no-op
			return this;
		}
		else {
			return new Slots(types.update(idx, type), captured, varargPosition);
		}
	}

	public Slots join(int idx, SlotType type) {
		return updateType(idx, getType(idx).join(type));
	}

	public Slots join(Slots that) {
		Check.notNull(that);
		Check.isEq(this.size(), that.size());

		Slots s = this;
		for (int i = 0; i < size(); i++) {
			s = s.join(i, that.getType(i));
		}

		for (int i = 0; i < size(); i++) {
			if (that.isCaptured(i)) {
				s = s.capture(i);
			}
		}

		return s;
	}

	public Slots consumeVarargs() {
		return new Slots(types, captured, -1);
	}

	public Slots setVarargs(int position) {
		Check.nonNegative(position);
//		Check.isFalse(hasVarargs());

		Slots s = this.hasVarargs() ? this.consumeVarargs() : this;

		for (int i = position; i < size(); i++) {
			Check.isFalse(s.isCaptured(i));  // FIXME
			s = s.updateType(i, SlotType.NIL);
		}

		return new Slots(s.types, s.captured, position);
	}

}
