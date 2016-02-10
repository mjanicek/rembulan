package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.ArrayList;

public class ArgTypes {

	public final ReadOnlyArray<Type> types;
	public final boolean varargs;

	public ArgTypes(ReadOnlyArray<Type> types, boolean varargs) {
		Check.notNull(types);
		this.types = types;
		this.varargs = varargs;
	}

	public static ArgTypes init(int numArgs, boolean vararg) {
		Type[] types = new Type[numArgs];
		for (int i = 0; i < numArgs; i++) {
			types[i] = Type.ANY;
		}
		return new ArgTypes(ReadOnlyArray.wrap(types), vararg);
	}

	private static final ArgTypes EMPTY_FIXED = new ArgTypes(ReadOnlyArray.wrap(new Type[0]), false);
	private static final ArgTypes EMPTY_VARARG = new ArgTypes(ReadOnlyArray.wrap(new Type[0]), true);

	public static ArgTypes empty() {
		return EMPTY_FIXED;
	}

	public static ArgTypes vararg() {
		return EMPTY_VARARG;
	}

	public static ArgTypes of(Type... fixed) {
		return new ArgTypes(ReadOnlyArray.wrap(fixed), false);
	}

	public ArgTypes withVararg() {
		return hasVarargs() ? this : new ArgTypes(types, true);
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
			bld.append(Type.toString(types.get(i)));
		}
		if (varargs) {
			bld.append("+");
		}
		return bld.toString();
	}

	public ReadOnlyArray<Type> types() {
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

	public boolean isVarargOnly() {
		return types().isEmpty() && hasVarargs();
	}

	public Type get(int idx) {
		Check.nonNegative(idx);

		if (idx < types().size()) return types().get(idx);  // it's a fixed arg
		else if (hasVarargs()) return Type.ANY;  // it's a vararg
		else return Type.NIL;  // it's not there
	}

	public boolean isSubsumedBy(ArgTypes that) {
		Check.notNull(that);

		// that is more general than this

		for (int i = 0; i < Math.max(this.types().size(), that.types().size()); i++) {
			if (!this.get(i).isSubtypeOf(that.get(i))) {
				return false;
			}
		}

		return that.hasVarargs() || !this.hasVarargs();
	}

	public ArgTypes join(ArgTypes that) {
		Check.notNull(that);

		ArrayList<Type> fix = new ArrayList<>();

		for (int i = 0; i < Math.max(this.types().size(), that.types().size()); i++) {
			fix.add(this.get(i).join(that.get(i)));
		}

		return new ArgTypes(ReadOnlyArray.fromCollection(Type.class, fix), this.hasVarargs() || that.hasVarargs());
	}

	// returns null to indicate that no meet exists
	public ArgTypes meet(ArgTypes that) {
		Check.notNull(that);

		ArrayList<Type> fix = new ArrayList<>();

		for (int i = 0; i < Math.max(this.types().size(), that.types().size()); i++) {
			Type m = this.get(i).meet(that.get(i));
			if (m != null) {
				fix.add(m);
			}
			else {
				return null;
			}
		}

		return new ArgTypes(ReadOnlyArray.fromCollection(Type.class, fix), this.hasVarargs() && that.hasVarargs());
	}

}
