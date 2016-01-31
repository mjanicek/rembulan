package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.compiler.gen.Slots.SlotType;
import net.sandius.rembulan.compiler.gen.block.LuaInstruction.NumOpType;
import net.sandius.rembulan.lbc.Prototype;

import java.util.Objects;

public abstract class BinaryOperation extends Linear {

	public final Prototype prototype;

	public final int dest;
	public final int b;
	public final int c;

	public BinaryOperation(Prototype prototype, int dest, int b, int c) {
		this.prototype = Objects.requireNonNull(prototype);
		this.dest = dest;
		this.b = b;
		this.c = c;
	}

	protected abstract String name();

	@Override
	public String toString() {
		return name() + opType(inSlots()).toSuffix() + "(" + dest + "," + b + "," + c + ")";
	}

	protected Slots.SlotType slotType(Slots in, int idx) {
		return idx < 0 ? LuaInstruction.constantType(prototype.getConstants().get(-idx - 1)) : in.getType(idx);
	}

	protected abstract NumOpType opType(SlotType l, SlotType r);

	protected NumOpType opType(Slots in) {
		return opType(slotType(in, b), slotType(in, c));
	}

	private static NumOpType mayBeInteger(SlotType l, SlotType r) {
		if (l.isNumber() && r.isNumber()) {
			if (l == SlotType.NUMBER_INTEGER && r == SlotType.NUMBER_INTEGER) return NumOpType.Integer;
			else if (l == SlotType.NUMBER_FLOAT || r == SlotType.NUMBER_FLOAT) return NumOpType.Float;
			else return NumOpType.Number;
		}
		else {
			return NumOpType.Any;
		}
	}

	private static NumOpType mustBeFloat(SlotType l, SlotType r) {
		if (l.isNumber() && r.isNumber()) return NumOpType.Float;
		else return NumOpType.Any;
	}

	private static NumOpType mustBeInteger(SlotType l, SlotType r) {
		if (l.isNumber() && r.isNumber()) return NumOpType.Integer;
		else return NumOpType.Any;
	}

	@Override
	protected Slots effect(Slots in) {
		switch (opType(in)) {
			case Integer:  return in.updateType(dest, Slots.SlotType.NUMBER_INTEGER);
			case Float:    return in.updateType(dest, Slots.SlotType.NUMBER_FLOAT);
			case Number:   return in.updateType(dest, Slots.SlotType.NUMBER);
			case Any:
			default:       return in.updateType(dest, Slots.SlotType.ANY);
		}
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
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
		protected NumOpType opType(Slots.SlotType l, Slots.SlotType r) {
			return mustBeInteger(l, r);
		}

	}

}
