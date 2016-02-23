package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.gen.Origin;
import net.sandius.rembulan.compiler.gen.Slot;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.types.Type;

public abstract class UnaryOperation extends Linear {

	public final int r_dest;
	public final int r_arg;

	private UnaryOperation(int a, int b) {
		this.r_dest = a;
		this.r_arg = b;
	}

	protected abstract String name();

	protected abstract Type resultType(Type in);

	@Override
	public String toString() {
		Type rt = resultType(inSlots().getType(r_arg));
		String suffix = rt != LuaTypes.ANY ? "_" + rt : "";
		return name() + suffix + "(" + r_dest + "," + r_arg + ")";
	}

	@Override
	protected SlotState effect(SlotState s) {
		return s.update(r_dest, Slot.of(new Origin.Computed(), resultType(s.getType(r_arg))));
	}

	public static class Unm extends UnaryOperation {

		public Unm(int dest, int b) {
			super(dest, b);
		}

		@Override
		protected String name() {
			return "UNM";
		}

		@Override
		protected Type resultType(Type in) {
			return in.isSubtypeOf(LuaTypes.NUMBER) ? in : LuaTypes.ANY;
		}

	}

	public static class BNot extends UnaryOperation {

		public BNot(int dest, int b) {
			super(dest, b);
		}

		@Override
		protected String name() {
			return "BNOT";
		}

		@Override
		protected Type resultType(Type in) {
			// TODO: for constants, we could determine whether the argument is coercible to integer -> need access ot it
			return in == LuaTypes.NUMBER_INTEGER ? LuaTypes.NUMBER_INTEGER : LuaTypes.ANY;
		}

	}

	public static class Not extends UnaryOperation {

		public Not(int dest, int b) {
			super(dest, b);
		}

		@Override
		protected String name() {
			return "NOT";
		}

		@Override
		protected Type resultType(Type in) {
			return LuaTypes.BOOLEAN;
		}

	}

	public static class Len extends UnaryOperation {

		public Len(int dest, int b) {
			super(dest, b);
		}

		@Override
		protected String name() {
			return "LEN";
		}

		@Override
		protected Type resultType(Type in) {
			return in == LuaTypes.STRING ? LuaTypes.NUMBER_INTEGER : LuaTypes.ANY;
		}

	}

}
