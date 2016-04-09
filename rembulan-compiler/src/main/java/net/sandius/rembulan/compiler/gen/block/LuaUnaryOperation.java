package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.gen.Origin;
import net.sandius.rembulan.compiler.gen.Slot;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.types.Type;

public abstract class LuaUnaryOperation extends Linear implements LuaInstruction {

	public final int r_dest;
	public final int r_arg;

	private LuaUnaryOperation(int a, int b) {
		this.r_dest = a;
		this.r_arg = b;
	}

	protected abstract String name();

	protected abstract Type resultType(Type in);

	@Override
	public String toString() {
		Type rt = resultType(inSlots().typeAt(r_arg));
		String suffix = rt != LuaTypes.ANY ? "_" + rt : "";
		return name() + suffix + "(" + r_dest + "," + r_arg + ")";
	}

	@Override
	protected SlotState effect(SlotState s) {
		return s.update(r_dest, Slot.of(Origin.Computed.in(this), resultType(s.typeAt(r_arg))));
	}

	public static class Unm extends LuaUnaryOperation {

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

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitUnm(this, inSlots(), r_dest, r_arg);
		}

	}

	public static class BNot extends LuaUnaryOperation {

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
			return in.isSubtypeOf(LuaTypes.NUMBER_INTEGER) ? LuaTypes.NUMBER_INTEGER : LuaTypes.ANY;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitBNot(this, inSlots(), r_dest, r_arg);
		}

	}

	public static class Not extends LuaUnaryOperation {

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

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitNot(this, inSlots(), r_dest, r_arg);
		}

	}

	public static class Len extends LuaUnaryOperation {

		public Len(int dest, int b) {
			super(dest, b);
		}

		@Override
		protected String name() {
			return "LEN";
		}

		@Override
		protected Type resultType(Type in) {
			return in.isSubtypeOf(LuaTypes.STRING) ? LuaTypes.NUMBER_INTEGER : LuaTypes.ANY;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitLen(this, inSlots(), r_dest, r_arg);
		}

	}

}
