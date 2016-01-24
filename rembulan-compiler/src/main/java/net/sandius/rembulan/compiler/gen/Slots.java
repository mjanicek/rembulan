package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.Arrays;

public class Slots {

	public enum SlotState {

		FRESH,
		CAPTURED;

		public boolean isFresh() {
			return this == FRESH;
		}

		public boolean isCaptured() {
			return this == CAPTURED;
		}

	}

	public enum SlotType {

		ANY,
		NIL,
		BOOLEAN,
		NUMBER,
		NUMBER_INTEGER,
		NUMBER_FLOAT,
		STRING,
		TABLE,
		THREAD,
		FUNCTION;

		// TODO: number-as-string, string-as-number, true, false, actual constant values?

		public boolean isNumber() {
			return this == NUMBER || this == NUMBER_INTEGER || this == NUMBER_FLOAT;
		}

		public SlotType join(SlotType that) {
			Check.notNull(that);
			if (this == that) {
				return this;
			}
			else {
				if (this.isNumber() && that.isNumber()) {
					return NUMBER;
				}
				else {
					return ANY;
				}
			}
		}

	}

	private final ReadOnlyArray<SlotState> states;
	private final ReadOnlyArray<SlotType> types;

	private Slots(ReadOnlyArray<SlotState> states, ReadOnlyArray<SlotType> types) {
		Check.notNull(states);
		Check.notNull(types);
		Check.isEq(states.size(), types.size());

		this.states = states;
		this.types = types;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Slots that = (Slots) o;

		return states.shallowEquals(that.states) && types.shallowEquals(that.types);
	}

	@Override
	public int hashCode() {
		int result = states.shallowHashCode();
		result = 31 * result + states.shallowHashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		for (int i = 0; i < size(); i++) {
			SlotState state = getState(i);
			SlotType type = getType(i);

			if (bld.length() > 0) bld.append(' ');
			switch (state) {
				case FRESH: bld.append('-'); break;
				case CAPTURED: bld.append('@'); break;
			}
			switch (type) {
				case ANY: bld.append('*'); break;
				case NIL: bld.append('0'); break;
				case BOOLEAN: bld.append('B'); break;
				case NUMBER: bld.append('n'); break;
				case NUMBER_INTEGER: bld.append("nI"); break;
				case NUMBER_FLOAT: bld.append("nF"); break;
				case STRING: bld.append('S'); break;
				case FUNCTION: bld.append('F'); break;
				case TABLE: bld.append('T'); break;
				case THREAD: bld.append('R'); break;
				default: bld.append('?'); break;
			}
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

		return new Slots(ReadOnlyArray.wrap(states), ReadOnlyArray.wrap(types));
	}

	public int size() {
		return states.size();
	}

	public ReadOnlyArray<SlotState> states() {
		return states;
	}

	public ReadOnlyArray<SlotType> types() {
		return types;
	}

	public SlotState getState(int idx) {
		return states.get(idx);
	}

	public Slots updateState(int idx, SlotState to) {
		Check.notNull(to);

		if (getState(idx).equals(to)) {
			// no-op
			return this;
		}
		else {
			return new Slots(states.update(idx, to), types);
		}
	}

	public Slots capture(int idx) {
		return updateState(idx, SlotState.CAPTURED);
	}

	public Slots freshen(int idx) {
		return updateState(idx, SlotState.FRESH);
	}

	public SlotType getType(int idx) {
		return types.get(idx);
	}

	public Slots updateType(int idx, SlotType type) {
		Check.notNull(type);

		if (getType(idx).equals(type)) {
			// no-op
			return this;
		}
		else {
			return new Slots(states, types.update(idx, type));
		}
	}

	public Slots join(int idx, SlotType type) {
		return updateType(idx, getType(idx).join(type));
	}

}
