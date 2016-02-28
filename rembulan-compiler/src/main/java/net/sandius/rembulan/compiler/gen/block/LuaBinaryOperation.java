package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.CompilationContext;
import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.gen.Origin;
import net.sandius.rembulan.compiler.gen.Slot;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.gen.block.LuaInstruction.NumOpType;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;

public abstract class LuaBinaryOperation extends Linear {

	public final Prototype prototype;
	public final CompilationContext context;

	public final int r_dest;
	public final int rk_left;
	public final int rk_right;

	public LuaBinaryOperation(Prototype prototype, CompilationContext context, int a, int b, int c) {
		this.prototype = Check.notNull(prototype);
		this.context = Check.notNull(context);
		this.r_dest = a;
		this.rk_left = LuaInstruction.registerOrConst(b);
		this.rk_right = LuaInstruction.registerOrConst(c);
	}

	protected abstract String name();

	@Override
	public String toString() {
		return name() + opType(inSlots()).toSuffix() + "(" + r_dest + "," + rk_left + "," + rk_right + ")";
	}

	protected Type slotType(SlotState s, int idx) {
		return idx < 0 ? context.constType(prototype, -idx - 1) : s.typeAt(idx);
	}

	protected abstract NumOpType opType(Type l, Type r);

	protected NumOpType opType(SlotState s) {
		return opType(slotType(s, rk_left), slotType(s, rk_right));
	}

	private static NumOpType mayBeInteger(Type l, Type r) {
		if (l.isSubtypeOf(LuaTypes.NUMBER) && r.isSubtypeOf(LuaTypes.NUMBER)) {
			if (l.isSubtypeOf(LuaTypes.NUMBER_INTEGER) && r.isSubtypeOf(LuaTypes.NUMBER_INTEGER)) return NumOpType.Integer;
			else if (l.isSubtypeOf(LuaTypes.NUMBER_FLOAT) || r.isSubtypeOf(LuaTypes.NUMBER_FLOAT)) return NumOpType.Float;
			else return NumOpType.Number;
		}
		else {
			return NumOpType.Any;
		}
	}

	private static NumOpType mustBeFloat(Type l, Type r) {
		if (l.isSubtypeOf(LuaTypes.NUMBER) && r.isSubtypeOf(LuaTypes.NUMBER)) return NumOpType.Float;
		else return NumOpType.Any;
	}

	private static NumOpType mustBeInteger(Type l, Type r) {
		if (l.isSubtypeOf(LuaTypes.NUMBER) && r.isSubtypeOf(LuaTypes.NUMBER)) return NumOpType.Integer;
		else return NumOpType.Any;
	}

	@Override
	protected SlotState effect(SlotState s) {
		return s.update(r_dest, Slot.of(Origin.Computed.in(this), opType(s).toSlotType()));
	}

	public static class Add extends LuaBinaryOperation {

		public Add(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "ADD";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mayBeInteger(l, r);
		}

	}

	public static class Sub extends LuaBinaryOperation {

		public Sub(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "SUB";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mayBeInteger(l, r);
		}

	}

	public static class Mul extends LuaBinaryOperation {

		public Mul(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "MUL";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mayBeInteger(l, r);
		}

	}

	public static class Mod extends LuaBinaryOperation {

		public Mod(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "MOD";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mayBeInteger(l, r);
		}

	}

	public static class Pow extends LuaBinaryOperation {

		public Pow(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "POW";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeFloat(l, r);
		}

	}

	public static class Div extends LuaBinaryOperation {

		public Div(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "DIV";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeFloat(l, r);
		}

	}

	public static class IDiv extends LuaBinaryOperation {

		public IDiv(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "IDIV";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mayBeInteger(l, r);
		}

	}

	public static class BAnd extends LuaBinaryOperation {

		public BAnd(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "BAND";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeInteger(l, r);
		}

	}

	public static class BOr extends LuaBinaryOperation {

		public BOr(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "BOR";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeInteger(l, r);
		}

	}

	public static class BXor extends LuaBinaryOperation {

		public BXor(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "BXOR";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeInteger(l, r);
		}

	}

	public static class Shl extends LuaBinaryOperation {

		public Shl(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "SHL";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeInteger(l, r);
		}

	}

	public static class Shr extends LuaBinaryOperation {

		public Shr(Prototype prototype, CompilationContext context, int dest, int b, int c) {
			super(prototype, context, dest, b, c);
		}

		@Override
		protected String name() {
			return "SHR";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeInteger(l, r);
		}

	}

}
