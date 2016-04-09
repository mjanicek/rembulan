package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Origin;
import net.sandius.rembulan.compiler.gen.PrototypeContext;
import net.sandius.rembulan.compiler.gen.Slot;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.util.Check;

import static net.sandius.rembulan.compiler.gen.block.LuaUtils.prefix;

public abstract class LuaBinaryOperation extends Linear implements LuaInstruction {

	public final PrototypeContext context;

	public final int r_dest;
	public final int rk_left;
	public final int rk_right;

	public LuaBinaryOperation(PrototypeContext context, int a, int b, int c) {
		this.context = Check.notNull(context);
		this.r_dest = a;
		this.rk_left = LuaUtils.registerOrConst(b);
		this.rk_right = LuaUtils.registerOrConst(c);
	}

	protected abstract String name();

	@Override
	public String toString() {
		return prefix(this) + name() + opType(inSlots()).toSuffix() + "(" + r_dest + "," + rk_left + "," + rk_right + ")";
	}

	protected static Type slotType(PrototypeContext context, SlotState slots, int rk) {
		return rk < 0 ? context.constType(-rk - 1) : slots.typeAt(rk);
	}

	protected abstract StaticMathImplementation math();

//	protected abstract NumOpType opType(Type l, Type r);

	protected NumOpType opType(SlotState s) {
		return math().opType(slotType(context, s, rk_left), slotType(context, s, rk_right));
	}

	@Override
	protected SlotState effect(SlotState s) {
		return s.update(r_dest, Slot.of(Origin.Computed.in(this), opType(s).toSlotType()));
	}

	@Override
	public boolean needsResumePoint() {
		switch (opType(inSlots())) {
			case Any: return true;
			default: return false;
		}
	}

	public static class Add extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MAY_BE_INTEGER;

		public Add(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "ADD";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitAdd(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

	public static class Sub extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MAY_BE_INTEGER;

		public Sub(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "SUB";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitSub(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

	public static class Mul extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MAY_BE_INTEGER;

		public Mul(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "MUL";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitMul(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

	public static class Mod extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MAY_BE_INTEGER;

		public Mod(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "MOD";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitMod(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

	public static class Pow extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MUST_BE_FLOAT;

		public Pow(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "POW";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitPow(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

	public static class Div extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MUST_BE_FLOAT;

		public Div(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "DIV";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitDiv(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

	public static class IDiv extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MAY_BE_INTEGER;

		public IDiv(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "IDIV";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitIDiv(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

	public static class BAnd extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MUST_BE_INTEGER;

		public BAnd(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "BAND";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitBAnd(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

	public static class BOr extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MUST_BE_INTEGER;

		public BOr(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "BOR";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitBOr(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

	public static class BXor extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MUST_BE_INTEGER;

		public BXor(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "BXOR";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitBXOr(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

	public static class Shl extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MUST_BE_INTEGER;

		public Shl(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "SHL";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitShl(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

	public static class Shr extends LuaBinaryOperation {

		public static final StaticMathImplementation MATH = StaticMathImplementation.MUST_BE_INTEGER;

		public Shr(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "SHR";
		}

		@Override
		protected StaticMathImplementation math() {
			return MATH;
		}

		@Override
		public void emit(CodeEmitter e) {
			e.codeVisitor().visitShr(this, inSlots(), r_dest, rk_left, rk_right);
		}

	}

}
