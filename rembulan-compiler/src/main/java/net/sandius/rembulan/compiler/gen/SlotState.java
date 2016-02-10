package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntSet;
import net.sandius.rembulan.util.ReadOnlyArray;

public class SlotState {

	private final ReadOnlyArray<Type> types;
	private final IntSet captured;
	private final int varargPosition;  // first index of varargs; if negative, no varargs in slots

	private SlotState(ReadOnlyArray<Type> types, IntSet captured, int varargPosition) {
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

		SlotState that = (SlotState) o;

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
			Type type = getType(i);

			if (isCaptured(i)) {
				bld.append('^');
			}
			bld.append(Type.toString(type));
		}

		if (varargPosition() >= 0) {
			bld.append("+");
		}

		return bld.toString();
	}

	public static SlotState init(int size) {
		Check.nonNegative(size);

		Type[] types = new Type[size];

		for (int i = 0; i < size; i++) {
			types[i] = Type.NIL;
		}

		return new SlotState(ReadOnlyArray.wrap(types), IntSet.empty(), -1);
	}

	public int size() {
		return types.size();
	}

	public ReadOnlyArray<Type> types() {
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

	public SlotState updateState(int idx, boolean cap) {
		Check.isTrue(isValidIndex(idx));

		if (isCaptured(idx) == cap) {
			// no-op
			return this;
		}
		else {
			return new SlotState(types, cap ? captured.plus(idx) : captured.minus(idx), varargPosition);
		}
	}

	public SlotState capture(int idx) {
		return updateState(idx, true);
	}

	public SlotState freshen(int idx) {
		return updateState(idx, false);
	}

	public Type getType(int idx) {
		Check.isTrue(isValidIndex(idx));
		return types.get(idx);
	}

	public SlotState updateType(int idx, Type type) {
		Check.notNull(type);
		Check.isTrue(isValidIndex(idx));

		if (getType(idx).equals(type)) {
			// no-op
			return this;
		}
		else {
			return new SlotState(types.update(idx, type), captured, varargPosition);
		}
	}

	public SlotState join(int idx, Type type) {
		return updateType(idx, getType(idx).join(type));
	}

	public SlotState join(SlotState that) {
		Check.notNull(that);
		Check.isEq(this.size(), that.size());

		SlotState s = this;
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

	public SlotState consumeVarargs() {
		return new SlotState(types, captured, -1);
	}

	public SlotState setVarargs(int position) {
		Check.nonNegative(position);
//		Check.isFalse(hasVarargs());

		SlotState s = this.hasVarargs() ? this.consumeVarargs() : this;

		for (int i = position; i < size(); i++) {
			Check.isFalse(s.isCaptured(i));  // FIXME
			s = s.updateType(i, Type.NIL);
		}

		return new SlotState(s.types, s.captured, position);
	}

}
