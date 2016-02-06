package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;

public class Slots {

	private enum SlotState {

		FRESH,
		CAPTURED;

	}

	private final ReadOnlyArray<SlotState> states;
	private final ReadOnlyArray<SlotType> types;
	private final int varargPosition;  // first index of varargs; if negative, no varargs in slots

	private Slots(ReadOnlyArray<SlotState> states, ReadOnlyArray<SlotType> types, int varargPosition) {
		Check.notNull(states);
		Check.notNull(types);
		Check.isEq(states.size(), types.size());

		int size = states.size();
		if (varargPosition >= 0) Check.inRange(varargPosition, 0, size - 1);

		this.states = states;
		this.types = types;
		this.varargPosition = varargPosition;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Slots that = (Slots) o;

		// FIXME: vararg position!

		return states.shallowEquals(that.states) && types.shallowEquals(that.types);
	}

	@Override
	public int hashCode() {
		// FIXME: vararg position!

		int result = states.shallowHashCode();
		result = 31 * result + states.shallowHashCode();
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

		SlotState[] states = new SlotState[size];
		SlotType[] types = new SlotType[size];

		for (int i = 0; i < size; i++) {
			states[i] = SlotState.FRESH;
			types[i] = SlotType.NIL;
		}

		return new Slots(ReadOnlyArray.wrap(states), ReadOnlyArray.wrap(types), -1);
	}

	public int size() {
		return states.size();
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
		return states.get(idx) == SlotState.CAPTURED;
	}

	public Slots updateState(int idx, boolean captured) {
		Check.isTrue(isValidIndex(idx));

		if (isCaptured(idx) == captured) {
			// no-op
			return this;
		}
		else {
			return new Slots(states.update(idx, captured ? SlotState.CAPTURED : SlotState.FRESH), types, varargPosition);
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
			return new Slots(states, types.update(idx, type), varargPosition);
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
		return new Slots(states, types, -1);
	}

	public Slots setVarargs(int position) {
		Check.nonNegative(position);
//		Check.isFalse(hasVarargs());

		Slots s = this.hasVarargs() ? this.consumeVarargs() : this;

		for (int i = position; i < size(); i++) {
			Check.isFalse(s.isCaptured(i));  // FIXME
			s = s.updateType(i, SlotType.NIL);
		}

		return new Slots(s.states, s.types, position);
	}

}
