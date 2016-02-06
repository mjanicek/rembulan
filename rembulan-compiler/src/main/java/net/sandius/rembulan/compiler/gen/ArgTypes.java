package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;

public class ArgTypes extends ReturnType {

	public final ReadOnlyArray<SlotType> types;
	public final boolean varargs;

	public ArgTypes(ReadOnlyArray<SlotType> types, boolean varargs) {
		Check.notNull(types);
		this.types = types;
		this.varargs = varargs;
	}

	public static ArgTypes init(int numArgs, boolean vararg) {
		SlotType[] types = new SlotType[numArgs];
		for (int i = 0; i < numArgs; i++) {
			types[i] = SlotType.ANY;
		}
		return new ArgTypes(ReadOnlyArray.wrap(types), vararg);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ArgTypes that = (ArgTypes) o;

		if (varargs != that.varargs) return false;
		return types.equals(that.types);
	}

	@Override
	public int hashCode() {
		int result = types.shallowHashCode();
		result = 31 * result + (varargs ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		for (int i = 0; i < types.size(); i++) {
			bld.append(SlotType.toString(types.get(i)));
		}
		if (varargs) {
			bld.append("+");
		}
		return bld.toString();
	}

	public ReadOnlyArray<SlotType> types() {
		return types;
	}

	public boolean hasVarargs() {
		return varargs;
	}

	public Slots toSlots(int size) {
		Slots s = Slots.init(size);
		for (int i = 0; i < types().size(); i++) {
			s = s.updateType(i, types().get(i));
		}
//		if (hasVarargs()) {
//			s = s.setVarargs(types().size());
//		}
		return s;
	}

}
