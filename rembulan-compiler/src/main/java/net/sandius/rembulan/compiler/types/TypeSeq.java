package net.sandius.rembulan.compiler.types;

import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.PartialOrderComparisonResult;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.ArrayList;

public class TypeSeq implements GradualTypeLike<TypeSeq> {

	protected final ReadOnlyArray<Type> fixed;
	protected final Type tailType;

	public TypeSeq(ReadOnlyArray<Type> fixed, Type tailType) {
		Check.notNull(fixed);
		Check.notNull(tailType);
		this.fixed = fixed;
		this.tailType = tailType;
	}

	@Deprecated
	public TypeSeq(ReadOnlyArray<Type> fixed, boolean varargs) {
		this(fixed, varargs ? LuaTypes.ANY : LuaTypes.NIL);
	}

//	private static final TypeSeq EMPTY_FIXED = new TypeSeq(ReadOnlyArray.wrap(new Type[0]), false);
//	private static final TypeSeq EMPTY_VARARG = new TypeSeq(ReadOnlyArray.wrap(new Type[0]), true);

	public static TypeSeq empty() {
//		return EMPTY_FIXED;
		return of();
	}

	public static TypeSeq vararg() {
//		return EMPTY_VARARG;
		return of().withVararg();
	}

	public static TypeSeq of(Type... fixed) {
		return new TypeSeq(ReadOnlyArray.wrap(fixed), false);
	}

	public TypeSeq withVararg() {
		return new TypeSeq(fixed, LuaTypes.ANY);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TypeSeq that = (TypeSeq) o;

		return this.tailType.equals(that.tailType) && this.fixed.equals(that.fixed);
	}

	@Override
	public int hashCode() {
		int result = fixed.shallowHashCode();
		result = 31 * result + tailType.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		for (int i = 0; i < fixed.size(); i++) {
			bld.append(fixed.get(i).toString());
		}

		if (tailType.equals(LuaTypes.ANY)) {
			bld.append("+");
		}
		else if (tailType.equals(LuaTypes.NIL)) {
			// do nothing
		}
		else {
			bld.append(tailType.toString()).append("*");
		}
		return bld.toString();
	}

	public ReadOnlyArray<Type> fixed() {
		return fixed;
	}

	public Type get(int idx) {
		Check.nonNegative(idx);
		return idx < fixed().size() ? fixed().get(idx) : tailType;
	}

	public boolean isSubsumedBy(TypeSeq that) {
		Check.notNull(that);

		// that is more general than this

		for (int i = 0; i < Math.max(this.fixed().size(), that.fixed().size()); i++) {
			if (!this.get(i).isSubtypeOf(that.get(i))) {
				return false;
			}
		}

		return this.tailType.isSubtypeOf(that.tailType);
	}

	public TypeSeq join(TypeSeq that) {
		Check.notNull(that);

		ArrayList<Type> fix = new ArrayList<>();

		for (int i = 0; i < Math.max(this.fixed().size(), that.fixed().size()); i++) {
			Type j = this.get(i).join(that.get(i));
			if (j != null) {
				fix.add(j);
			}
			else {
				return null;
			}
		}

		Type tt = this.tailType.join(that.tailType);

		if (tt != null) {
			return new TypeSeq(ReadOnlyArray.fromCollection(Type.class, fix), tt);
		}
		else {
			return null;
		}
	}

	// returns null to indicate that no meet exists
	public TypeSeq meet(TypeSeq that) {
		Check.notNull(that);

		ArrayList<Type> fix = new ArrayList<>();

		for (int i = 0; i < Math.max(this.fixed().size(), that.fixed().size()); i++) {
			Type m = this.get(i).meet(that.get(i));
			if (m != null) {
				fix.add(m);
			}
			else {
				return null;
			}
		}

		Type tt = this.tailType.meet(that.tailType);

		if (tt != null) {
			return new TypeSeq(ReadOnlyArray.fromCollection(Type.class, fix), tt);
		}
		else {
			return null;
		}
	}

	// Let < be the subtyping relation, and if seq is a TypeSeq and i is an index (a non-negative
	// integer), seq[i] is a shortcut for seq.get(i). We'll say that a type u is comparable
	// to type v iff (u == v || u < v || v < u), and not comparable otherwise.
	//
	// Then:
	// Returns NOT_COMPARABLE iff there is an index i such that this[i] is not comparable to that[i];
	// Returns EQUAL iff for all i, this[i] == that[i];
	// Returns LESSER_THAN iff there is an index i such that for all j < i, this[j] == that[j]
	//   and this[i] < that[i] and for all k, this[k] is comparable to that[k];
	// Returns GREATER_THAN iff there is an index i such that for all j < i, this[j] == that[j]
	//   and that[i] < this[i] and for all k, this[k] is comparable to that[k].
	//
	// If this.isSubsumedBy(that) then this.comparePointwiseTo(that) == EQUAL || this.comparePointwiseTo(that) == LESSER_THAN;
	// Please note that this is an implication: the opposite direction does *not* in general hold.
	public PartialOrderComparisonResult comparePointwiseTo(TypeSeq that) {
		Check.notNull(that);

		int len = Math.max(this.fixed().size(), that.fixed().size());

		PartialOrderComparisonResult result = null;

		for (int i = 0; i < len; i++) {
			PartialOrderComparisonResult r = this.get(i).compareTo(that.get(i));

			if (!r.isDefined()) {
				return PartialOrderComparisonResult.NOT_COMPARABLE;
			}

			if (result == null && r != PartialOrderComparisonResult.EQUAL) {
				result = r;
			}
		}

		PartialOrderComparisonResult tr =  this.tailType.compareTo(that.tailType);

		if (result != null && tr.isDefined()) {
			// tr doesn't ruin the result
			return result;
		}
		else {
			return tr;
		}
	}

	@Override
	public boolean isConsistentWith(TypeSeq that) {
		Check.notNull(that);

		for (int i = 0; i < Math.max(this.fixed().size(), that.fixed().size()); i++) {
			if (!this.get(i).isConsistentWith(that.get(i))) {
				return false;
			}
		}

		return this.tailType.isConsistentWith(that.tailType);
	}

	public TypeSeq restrict(TypeSeq that) {
		Check.notNull(that);

		ArrayList<Type> ts = new ArrayList<>();

		for (int i = 0; i < Math.max(this.fixed().size(), that.fixed().size()); i++) {
			ts.add(this.get(i).restrict(that.get(i)));
		}

		return new TypeSeq(ReadOnlyArray.fromCollection(Type.class, ts), this.tailType.restrict(that.tailType));
	}

	@Override
	public boolean isConsistentSubtypeOf(TypeSeq that) {
		Check.notNull(that);

		for (int i = 0; i < Math.max(this.fixed().size(), that.fixed().size()); i++) {
			if (!this.get(i).isConsistentSubtypeOf(that.get(i))) {
				return false;
			}
		}

		return this.tailType.isConsistentSubtypeOf(that.tailType);
	}

}
