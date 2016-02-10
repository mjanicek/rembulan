package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Type;
import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.compiler.gen.block.LuaInstruction.NumOpType;
import net.sandius.rembulan.lbc.Prototype;

import java.util.Objects;

public abstract class BinaryOperation extends Linear {

	public final Prototype prototype;

	public final int r_dest;
	public final int rk_left;
	public final int rk_right;

	public BinaryOperation(Prototype prototype, int a, int b, int c) {
		this.prototype = Objects.requireNonNull(prototype);
		this.r_dest = a;
		this.rk_left = LuaInstruction.registerOrConst(b);
		this.rk_right = LuaInstruction.registerOrConst(c);
	}

	protected abstract String name();

	@Override
	public String toString() {
		return name() + opType(inSlots()).toSuffix() + "(" + r_dest + "," + rk_left + "," + rk_right + ")";
	}

	protected Type slotType(Slots s, int idx) {
		return idx < 0 ? LuaInstruction.constantType(prototype.getConstants().get(-idx - 1)) : s.getType(idx);
	}

	protected abstract NumOpType opType(Type l, Type r);

	protected NumOpType opType(Slots s) {
		return opType(slotType(s, rk_left), slotType(s, rk_right));
	}

	private static NumOpType mayBeInteger(Type l, Type r) {
		if (l.isSubtypeOf(Type.NUMBER) && r.isSubtypeOf(Type.NUMBER)) {
			if (l == Type.NUMBER_INTEGER && r == Type.NUMBER_INTEGER) return NumOpType.Integer;
			else if (l == Type.NUMBER_FLOAT || r == Type.NUMBER_FLOAT) return NumOpType.Float;
			else return NumOpType.Number;
		}
		else {
			return NumOpType.Any;
		}
	}

	private static NumOpType mustBeFloat(Type l, Type r) {
		if (l.isSubtypeOf(Type.NUMBER) && r.isSubtypeOf(Type.NUMBER)) return NumOpType.Float;
		else return NumOpType.Any;
	}

	private static NumOpType mustBeInteger(Type l, Type r) {
		if (l.isSubtypeOf(Type.NUMBER) && r.isSubtypeOf(Type.NUMBER)) return NumOpType.Integer;
		else return NumOpType.Any;
	}

	@Override
	protected Slots effect(Slots s) {
		return s.updateType(r_dest, opType(s).toSlotType());
	}

	public static class Add extends BinaryOperation {

		public Add(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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

	public static class Sub extends BinaryOperation {

		public Sub(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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

	public static class Mul extends BinaryOperation {

		public Mul(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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

	public static class Mod extends BinaryOperation {

		public Mod(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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

	public static class Pow extends BinaryOperation {

		public Pow(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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

	public static class Div extends BinaryOperation {

		public Div(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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

	public static class IDiv extends BinaryOperation {

		public IDiv(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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

	public static class BAnd extends BinaryOperation {

		public BAnd(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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

	public static class BOr extends BinaryOperation {

		public BOr(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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

	public static class BXor extends BinaryOperation {

		public BXor(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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

	public static class Shl extends BinaryOperation {

		public Shl(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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

	public static class Shr extends BinaryOperation {

		public Shr(Prototype prototype, int dest, int b, int c) {
			super(prototype, dest, b, c);
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
