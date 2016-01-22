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

	private final SlotState[] states;

	private Slots(SlotState[] states) {
		Check.notNull(states);
		this.states = states;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Slots slots = (Slots) o;
		return Arrays.deepEquals(states, slots.states);
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode(states);
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		for (SlotState state : states) {
			if (bld.length() > 0) bld.append(' ');
			switch (state) {
				case FRESH: bld.append('-'); break;
				case CAPTURED: bld.append('@'); break;
			}
		}
		return bld.toString();
	}

	public static Slots init(int size) {
		Check.nonNegative(size);

		SlotState[] states = new SlotState[size];

		for (int i = 0; i < size; i++) {
			states[i] = SlotState.FRESH;
		}

		return new Slots(states);
	}

	public int size() {
		return states.length;
	}

	public ReadOnlyArray<SlotState> states() {
		return ReadOnlyArray.wrap(states);
	}

	public SlotState get(int idx) {
		return states[idx];
	}

	public Slots update(int idx, SlotState to) {
		Check.notNull(to);

		if (get(idx).equals(to)) {
			// no-op
			return this;
		}
		else {
			SlotState[] statesCopy = Arrays.copyOf(states, states.length);
			statesCopy[idx] = to;
			return new Slots(statesCopy);
		}
	}

	public Slots capture(int idx) {
		return update(idx, SlotState.CAPTURED);
	}

	public Slots freshen(int idx) {
		return update(idx, SlotState.FRESH);
	}

}
