package net.sandius.rembulan.lbc.recompiler.gen;

import net.sandius.rembulan.compiler.analysis.types.LuaTypes;
import net.sandius.rembulan.compiler.analysis.types.Type;
import net.sandius.rembulan.util.Check;

public class Slot {

	protected final Origin origin;
	protected final Type type;

	public static final Slot NIL_SLOT = Slot.of(Origin.NilConstant.INSTANCE, LuaTypes.NIL);

	protected Slot(Origin origin, Type type) {
		this.origin = Check.notNull(origin);
		this.type = Check.notNull(type);
	}

	public static Slot of(Origin origin, Type type) {
		return new Slot(origin, type);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Slot that = (Slot) o;

		return origin.equals(that.origin) && type.equals(that.type);
	}

	@Override
	public int hashCode() {
		int result = origin.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return origin.toString() + ":" + type.toString();
	}

	public Origin origin() {
		return origin;
	}

	public Type type() {
		return type;
	}

}
