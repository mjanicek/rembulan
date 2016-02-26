package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.compiler.types.TypeSeq;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntSet;
import net.sandius.rembulan.util.ReadOnlyArray;

public class SlotState {

	private final ReadOnlyArray<Slot> fixedSlots;
	private final IntSet captured;
	private final int varargPosition;  // first index of varargs; if negative, no varargs in slots

	private SlotState(ReadOnlyArray<Slot> fixedSlots, IntSet captured, int varargPosition) {
		Check.notNull(fixedSlots);
		Check.notNull(captured);

		int size = fixedSlots.size();
		if (varargPosition >= 0) Check.inRange(varargPosition, 0, size - 1);

		Check.inRange(captured.size(), 0, size - 1);

		this.fixedSlots = fixedSlots;
		this.captured = captured;
		this.varargPosition = varargPosition;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SlotState that = (SlotState) o;

		// FIXME: vararg position!

		return fixedSlots.shallowEquals(that.fixedSlots) && this.captured.equals(that.captured);
	}

	@Override
	public int hashCode() {
		// FIXME: vararg position!

		int result = fixedSlots.shallowHashCode();
		result = 31 * result + captured.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return toString(",");
	}

	public String toString(String separator) {
		StringBuilder bld = new StringBuilder();

		int numRegularSlots = varargPosition() < 0 ? size() : Math.min(size(), varargPosition());

		for (int i = 0; i < numRegularSlots; i++) {
			Slot slot = slotAt(i);

			if (isCaptured(i)) {
				bld.append('{');
			}
			bld.append(slot.toString());
			if (isCaptured(i)) {
				bld.append('}');
			}

			if (i + 1 < numRegularSlots) {
				bld.append(separator);
			}
		}

		if (varargPosition() >= 0) {
			if (numRegularSlots > 0) {
				bld.append(separator);
			}
			bld.append("+");
		}

		return bld.toString();
	}

	public static SlotState init(int size) {
		Check.nonNegative(size);

		Slot[] slots = new Slot[size];

		for (int i = 0; i < size; i++) {
			slots[i] = Slot.of(Origin.entry(), LuaTypes.NIL);
		}

		return new SlotState(ReadOnlyArray.wrap(slots), IntSet.empty(), -1);
	}

	// ignores the vararg part
	public static SlotState fromFixedTypes(TypeSeq ts, int size) {
		SlotState slots = SlotState.init(size);

		for (int i = 0; i < Math.min(ts.fixed().size(), size); i++) {
			slots = slots.update(i, Slot.of(new Origin.Argument(i), ts.fixed().get(i)));
		}

		return slots;
	}

	public int size() {
		return fixedSlots.size();
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

	@Deprecated
	public Slot slotAt(int idx) {
		Check.isTrue(isValidIndex(idx));
		return fixedSlots.get(idx);
	}

	public Type typeAt(int idx) {
		return slotAt(idx).type();
	}

	public Origin originAt(int idx) {
		return slotAt(idx).origin();
	}

	public boolean isValidIndex(int idx) {
		return idx >= 0 && idx < size() && (varargPosition < 0 || idx < varargPosition);
	}

	public boolean isCaptured(int idx) {
		Check.isTrue(isValidIndex(idx));
		return captured.contains(idx);
	}

	public SlotState updateState(int idx, boolean cap) {
		Check.isTrue(isValidIndex(idx));

		if (isCaptured(idx) == cap) {
			// no-op
			return this;
		}
		else {
			return new SlotState(fixedSlots, cap ? captured.plus(idx) : captured.minus(idx), varargPosition);
		}
	}

	public SlotState capture(int idx) {
		return updateState(idx, true);
	}

	public SlotState freshen(int idx) {
		return updateState(idx, false);
	}

	public SlotState update(int idx, Origin origin, Type type) {
		return update(idx, Slot.of(origin, type));
	}

	@Deprecated
	public SlotState update(int idx, Slot slot) {
		Check.notNull(slot);
		Check.isTrue(isValidIndex(idx));

		if (slotAt(idx).equals(slot)) {
			// no-op
			return this;
		}
		else {
			return new SlotState(fixedSlots.update(idx, slot), captured, varargPosition);
		}
	}

	private SlotState merge(int idx, Origin origin, Type type) {
		return update(idx, originAt(idx).merge(origin), typeAt(idx).unionWith(type));
	}

	public SlotState merge(SlotState that) {
		Check.notNull(that);
		Check.isEq(this.size(), that.size());

		SlotState s = this;
		for (int i = 0; i < size(); i++) {
			s = s.merge(i, that.originAt(i), that.typeAt(i));
		}

		for (int i = 0; i < size(); i++) {
			if (that.isCaptured(i)) {
				s = s.capture(i);
			}
		}

		return s;
	}

	public SlotState consumeVarargs() {
		return new SlotState(fixedSlots, captured, -1);
	}

	public SlotState setVarargs(int position) {
		Check.nonNegative(position);
//		Check.isFalse(hasVarargs());

		SlotState s = this.hasVarargs() ? this.consumeVarargs() : this;

		for (int i = position; i < size(); i++) {
			Check.isFalse(s.isCaptured(i));  // FIXME
			s = s.update(i, Slot.NIL_SLOT);
		}

		return new SlotState(s.fixedSlots, s.captured, position);
	}

}
