package net.sandius.rembulan.compiler.types;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.PartialOrderComparisonResult;

public abstract class Type implements GradualTypeLike<Type> {

	protected Type() {
	}

	// TODO: number-as-string, string-as-number, true, false, actual constant values?

	// standard subtyping relation
	// must return true if this.equals(that).
	public abstract boolean isSubtypeOf(Type that);

	// consistency relation
	@Override
	public boolean isConsistentWith(Type that) {
		return this.restrict(that).equals(that.restrict(this));
	}

	public abstract Type restrict(Type that);

	// return true iff type(this) ~< type(that)
	@Override
	public boolean isConsistentSubtypeOf(Type that) {
		return this.restrict(that).isSubtypeOf(that.restrict(this));
	}

	// return the most specific type that is more general than both this and that,
	// or null if such type does not exist
	@Deprecated
	public abstract Type join(Type that);

	// return the most general type that is more specific than both this and that,
	// or null if such type does not exist
	@Deprecated
	public abstract Type meet(Type that);

	// return a type T such that this.isConsistentSubtypeOf(T) && that.isConsistentSubtypeOf(T)
	public Type unionWith(Type that) {
		return this.restrict(that).join(that.restrict(this));
	}

	// compare this to that, returning:
	//   EQUAL if this.equals(that);
	//   LESSER_THAN if this.isSubtypeOf(that) && !this.equals(that);
	//   GREATER_THAN if that.isSubtypeOf(this) && !this.equals(that);
	//   NOT_COMPARABLE if !this.isSubtypeOf(that) && !that.isSubtypeOf(that).
	public PartialOrderComparisonResult compareTo(Type that) {
		Check.notNull(that);

		if (this.isSubtypeOf(that)) {
			if (this.equals(that)) {
				return PartialOrderComparisonResult.EQUAL;
			}
			else {
				return PartialOrderComparisonResult.LESSER_THAN;
			}
		}
		else {
			if (that.isSubtypeOf(this)) {
				return PartialOrderComparisonResult.GREATER_THAN;
			}
			else {
				return PartialOrderComparisonResult.NOT_COMPARABLE;
			}
		}
	}

}
