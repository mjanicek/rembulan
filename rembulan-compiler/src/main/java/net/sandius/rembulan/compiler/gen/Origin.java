package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.lbc.PrototypePrinter;
import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public abstract class Origin {

	private Origin() {
		// not to be extended by the outside world
	}

	public Origin merge(Origin that) {
		Objects.requireNonNull(that);
		return this.equals(that) ? this : Multi.of(this, that);
	}

	public static class Argument extends Origin {

		public final int index;

		public Argument(int index) {
			Check.nonNegative(index);
			this.index = index;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Argument that = (Argument) o;

			return this.index == that.index;
		}

		@Override
		public int hashCode() {
			return index;
		}

		@Override
		public String toString() {
			return "arg_" + index;
		}

	}

	public static class Constant extends Origin {

		public final int index;

		public Constant(int index) {
			Check.nonNegative(index);
			this.index = index;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Constant that = (Constant) o;
			return this.index == that.index;
		}

		@Override
		public int hashCode() {
			return index;
		}

		@Override
		public String toString() {
			return "const_" + index;
		}

	}

	public static class NilConstant extends Origin {

		public static final NilConstant INSTANCE = new NilConstant();

		private NilConstant() {
		}

		@Override
		public String toString() {
			return "$nil";
		}

	}

	public static class BooleanConstant extends Origin {

		public final boolean value;

		public static final BooleanConstant TRUE = new BooleanConstant(true);
		public static final BooleanConstant FALSE = new BooleanConstant(false);

		private BooleanConstant(boolean value) {
			this.value = value;
		}

		public static BooleanConstant fromBoolean(boolean value) {
			return value ? TRUE : FALSE;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			BooleanConstant that = (BooleanConstant) o;

			return this.value == that.value;
		}

		@Override
		public int hashCode() {
			return (value ? 1 : 0);
		}

		@Override
		public String toString() {
			return "$" + value;
		}

	}

	public static class Closure extends Origin {

		public final Prototype prototype;

		public Closure(Prototype prototype) {
			this.prototype = Objects.requireNonNull(prototype);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Closure that = (Closure) o;

			return prototype.equals(that.prototype);
		}

		@Override
		public int hashCode() {
			return prototype.hashCode();
		}

		@Override
		public String toString() {
			return "closure_" + Integer.toHexString(System.identityHashCode(prototype));
		}

	}

	public static class Upvalue extends Origin {

		public final int index;

		public Upvalue(int index) {
			Check.nonNegative(index);
			this.index = index;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Upvalue that = (Upvalue) o;
			return this.index == that.index;
		}

		@Override
		public int hashCode() {
			return index;
		}

		@Override
		public String toString() {
			return "upvalue_" + index;
		}

	}

	public static class Computed extends Origin {

		private final Object where;

		private Computed(Object where) {
			Check.notNull(where);
			this.where = where;
		}

		public static Computed in(Object where) {
			return new Computed(where);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Computed that = (Computed) o;

			return where.equals(that.where);
		}

		@Override
		public int hashCode() {
			return where.hashCode();
		}

		@Override
		public String toString() {
			return "_" + Integer.toHexString(this.hashCode());
		}

	}

	public static class Multi extends Origin {

		private final Set<Origin> origins;

		private Multi(Set<Origin> origins) {
			Check.notNull(origins);
			this.origins = origins;
		}

		private static void addAll(Set<Origin> dest, Origin src) {
			if (src instanceof Multi) {
				for (Origin o : ((Multi) src).origins) {
					addAll(dest, o);
				}
			}
			else {
				dest.add(src);
			}
		}

		public static Origin of(Origin... origins) {
			Check.notNull(origins);
			Check.gt(origins.length, 1);

			Set<Origin> set = new HashSet<>();
			for (Origin o : origins) {
				addAll(set, o);
			}

			return new Multi(Collections.unmodifiableSet(set));
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Multi that = (Multi) o;

			return this.origins.equals(that.origins);
		}

		@Override
		public int hashCode() {
			return origins.hashCode();
		}

		@Override
		public String toString() {
			StringBuilder bld = new StringBuilder();
			bld.append("[");
			Iterator<Origin> it = origins.iterator();
			while (it.hasNext()) {
				Origin o = it.next();
				bld.append(o.toString());
				if (it.hasNext()) bld.append(";");
			}
			bld.append("]");

			return bld.toString();
		}
	}

	public static Entry entry() {
		return Entry.INSTANCE;
	}

	public static class Entry extends Origin {

		public static final Entry INSTANCE = new Entry();

		private Entry() {
			// not to be instantiated directly
		}

		@Override
		public String toString() {
			return "_";
		}

	}

	public static class Vararg extends Origin {

		public final int index;

		public Vararg(int index) {
			this.index = index;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Vararg that = (Vararg) o;

			return this.index == that.index;
		}

		@Override
		public int hashCode() {
			return index;
		}

		@Override
		public String toString() {
			return "vararg_" + index;
		}

	}

	public static class CallResult extends Origin {

		public final Object where;
		public final int index;

		public CallResult(Object where, int index) {
			this.where = Objects.requireNonNull(where);
			this.index = index;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			CallResult that = (CallResult) o;

			return index == that.index && where.equals(that.where);
		}

		@Override
		public int hashCode() {
			int result = where.hashCode();
			result = 31 * result + index;
			return result;
		}

		@Override
		public String toString() {
			return "call_" + Integer.toHexString(where.hashCode()) + "_" + index;
		}

	}

}
