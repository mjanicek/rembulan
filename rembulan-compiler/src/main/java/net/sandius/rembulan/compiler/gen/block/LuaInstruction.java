package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.ArgTypes;
import net.sandius.rembulan.compiler.gen.ReturnType;
import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.compiler.gen.Slots.SlotType;
import net.sandius.rembulan.lbc.OpCode;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.lbc.PrototypePrinter;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.Objects;

public class LuaInstruction {

	public static int registerOrConst(int i) {
		return OpCode.isK(i) ? -1 - OpCode.indexK(i) : i;
	}

	public static SlotType constantType(Object k) {
		if (k == null) return SlotType.NIL;
		else if (k instanceof Boolean) return SlotType.BOOLEAN;
		else if (k instanceof Double || k instanceof Float) return SlotType.NUMBER_FLOAT;
		else if (k instanceof Number) return SlotType.NUMBER_INTEGER;
		else if (k instanceof String) return SlotType.STRING;
		else {
			throw new IllegalStateException("Unknown constant: " + k);
		}
	}

	public static ArgTypes argTypesFromSlots(Slots s, int from, int count) {
		int num = count > 0 ? count - 1 : s.varargPosition() - from;

		SlotType[] args = new SlotType[num];
		for (int i = 0; i < num; i++) {
			args[i] = s.getType(from + i);
		}

		return new ArgTypes(ReadOnlyArray.wrap(args), count <= 0);
	}

	public enum NumOpType {
		Integer,
		Float,
		Number,
		Any;

		public SlotType toSlotType() {
			switch (this) {
				case Integer:  return SlotType.NUMBER_INTEGER;
				case Float:    return SlotType.NUMBER_FLOAT;
				case Number:   return SlotType.NUMBER;
				case Any:
				default:       return Slots.SlotType.ANY;
			}
		}

		public String toSuffix() {
			switch (this) {
				case Integer: return "_i";
				case Float:   return "_f";
				case Number:  return "_N";
				case Any:
				default:      return "";
			}
		}
	}



	public static class Move extends Linear {

		public final int r_dest;
		public final int r_src;

		public Move(int a, int b) {
			this.r_dest = a;
			this.r_src = b;
		}

		@Override
		public String toString() {
			return "MOVE(" + r_dest + "," + r_src + ")";
		}

		@Override
		protected Slots effect(Slots s) {
			return s.updateType(r_dest, s.getType(r_src));
		}

	}

	public static class LoadK extends Linear {

		public final Prototype prototype;

		public final int r_dest;
		public final int constIndex;

		public LoadK(Prototype prototype, int a, int bx) {
			this.prototype = Objects.requireNonNull(prototype);
			this.r_dest = a;
			this.constIndex = bx;
		}

		@Override
		public String toString() {
			return "LOADK(" + r_dest + "," + constIndex + ")";
		}

		@Override
		protected Slots effect(Slots s) {
			return s.updateType(r_dest, constantType(prototype.getConstants().get(constIndex)));
		}

	}

	public static class LoadBool extends Linear {

		public final int r_dest;
		public final boolean value;

		public LoadBool(int a, int b) {
			this.r_dest = a;
			this.value = (b != 0);
		}

		@Override
		public String toString() {
			return "LOADBOOL(" + r_dest + "," + value + ")";
		}

		@Override
		protected Slots effect(Slots s) {
			return s.updateType(r_dest, SlotType.BOOLEAN);
		}
	}

	public static class LoadNil extends Linear {

		public final int r_dest;
		public final int count;

		public LoadNil(int a, int b) {
			this.r_dest = a;
			this.count = b + 1;
		}

		@Override
		public String toString() {
			return "LOADNIL(" + r_dest + "," + (count - 1) + ")";
		}

		@Override
		protected Slots effect(Slots s) {
			for (int i = 0; i < count; i++) {
				s = s.updateType(r_dest + i, SlotType.NIL);
			}
			return s;
		}

	}

	public static class GetUpVal extends Linear {

		public final int r_dest;
		public final int upvalueIndex;

		public GetUpVal(int a, int b) {
			this.r_dest = a;
			this.upvalueIndex = b;
		}

		@Override
		public String toString() {
			return "GETUPVAL(" + r_dest + "," + upvalueIndex + ")";
		}

		@Override
		protected Slots effect(Slots s) {
			return s.updateType(r_dest, SlotType.ANY);
		}

	}

	public static class GetTabUp extends Linear {

		public final int r_dest;
		public final int upvalueIndex;
		public final int rk_key;

		public GetTabUp(int a, int b, int c) {
			this.r_dest = a;
			this.upvalueIndex = b;
			this.rk_key = registerOrConst(c);
		}

		@Override
		public String toString() {
			return "GETTABUP(" + r_dest + "," + upvalueIndex + "," + rk_key + ")";
		}

		@Override
		protected Slots effect(Slots s) {
			return s.updateType(r_dest, SlotType.ANY);
		}

	}

	public static class GetTable extends Linear {

		public final int r_dest;
		public final int r_tab;
		public final int rk_key;

		public GetTable(int a, int b, int c) {
			this.r_dest = a;
			this.r_tab = b;
			this.rk_key = registerOrConst(c);
		}

		@Override
		public String toString() {
			String suffix = inSlots().getType(r_tab) == SlotType.FUNCTION ? "_T" : "";
			return "GETTABLE" + suffix + "(" + r_dest + "," + r_tab + "," + rk_key + ")";
		}

		@Override
		protected Slots effect(Slots s) {
			return s.updateType(r_dest, SlotType.ANY);
		}

	}

	public static class SetTabUp extends Linear {

		public final int upvalueIndex;
		public final int rk_key;
		public final int rk_value;

		public SetTabUp(int a, int b, int c) {
			this.upvalueIndex = a;
			this.rk_key = registerOrConst(b);
			this.rk_value = registerOrConst(c);
		}

		@Override
		public String toString() {
			return "SETTABUP(" + upvalueIndex + "," + rk_key + "," + rk_value + ")";
		}

	}

	public static class SetUpVal extends Linear {

		public final int r_src;
		public final int upvalueIndex;

		public SetUpVal(int a, int b) {
			this.r_src = a;
			this.upvalueIndex = b;
		}

		@Override
		public String toString() {
			return "SETUPVAL(" + r_src + "," + upvalueIndex + ")";
		}

	}

	public static class SetTable extends Linear {

		public final int r_tab;
		public final int rk_key;
		public final int rk_value;

		public SetTable(int a, int b, int c) {
			this.r_tab = a;
			this.rk_key = registerOrConst(b);
			this.rk_value = registerOrConst(c);
		}

		@Override
		public String toString() {
			String suffix = (inSlots().getType(r_tab) == SlotType.TABLE ? "_T" : "");
			return "SETTABLE" + suffix + "(" + r_tab + "," + rk_key + "," + rk_value + ")";
		}

	}

	public static class NewTable extends Linear {

		public final int r_dest;
		public final int arraySize;
		public final int hashSize;

		public NewTable(int a, int b, int c) {
			this.r_dest = a;

			// TODO: process B and C encoding
			this.arraySize = b;
			this.hashSize = c;
		}

		@Override
		public String toString() {
			return "NEWTABLE(" + r_dest + ",array=" + arraySize + ",hash=" + hashSize + ")";
		}

		@Override
		protected Slots effect(Slots s) {
			return s.updateType(r_dest, SlotType.TABLE);
		}

	}

	public static class Self extends Linear {

		public final int r_dest;
		public final int r_self;
		public final int rk_key;

		public Self(int a, int b, int c) {
			this.r_dest = a;
			this.r_self = b;
			this.rk_key = registerOrConst(c);
		}

		@Override
		public String toString() {
			String suffix = inSlots().getType(r_self) == SlotType.TABLE ? "_T" : "";
			return "SELF" + suffix + "(" + r_dest + "," + r_self + "," + rk_key + ")";
		}

		@Override
		protected Slots effect(Slots s) {
			return s.updateType(r_dest + 1, s.getType(r_self))
					.updateType(r_dest, SlotType.ANY);
		}

	}

	public enum UnOpType {
		UNM, BNOT, NOT, LEN;
	}

	// TODO
	public static class UnOp extends Linear {

		public final Prototype prototype;

		public final UnOpType op;
		public final int dest;
		public final int b;

		public UnOp(Prototype prototype, UnOpType op, int dest, int b) {
			this.prototype = Objects.requireNonNull(prototype);
			this.op = Objects.requireNonNull(op);
			this.dest = dest;
			this.b = b;
		}

		@Override
		public String toString() {
			return op.toString() + "(" + dest + "," + b + ")";
		}

		@Override
		protected Slots effect(Slots s) {
			SlotType argType = b < 0 ? constantType(prototype.getConstants().get(-b - 1)) : s.getType(b);

			SlotType resultType = SlotType.ANY;  // assume we'll be calling a metamethod

			switch (op) {
				case UNM:
					if (argType.isNumber()) resultType = argType;
					break;

				case BNOT:
					if (argType == SlotType.NUMBER_INTEGER) resultType = argType;
					break;

				case NOT:
					resultType = SlotType.BOOLEAN;
					break;

				case LEN:
					if (argType == SlotType.STRING) resultType = SlotType.NUMBER_INTEGER;
					break;
			}

			return s.updateType(dest, resultType);
		}
	}

	public static class Concat extends Linear {

		public final int r_dest;
		public final int r_begin;
		public final int r_end;

		public Concat(int a, int b, int c) {
			this.r_dest = a;
			this.r_begin = b;
			this.r_end = c;
		}

		@Override
		public String toString() {
			String suffix = allStringable(inSlots()) ? "_S" : "";
			return "CONCAT" + suffix + "(" + r_dest + "," + r_begin + ".." + r_end + ")";
		}

		private boolean allStringable(Slots s) {
			for (int i = r_begin; i <= r_end; i++) {
				SlotType tpe = s.getType(i);
				if (!(tpe == SlotType.STRING || tpe.isNumber())) {
					return false;
				}
			}
			return true;
		}

		@Override
		protected Slots effect(Slots s) {
			return s.updateType(r_dest, allStringable(s) ? SlotType.STRING : SlotType.ANY);
		}

	}

	// No explicit node for jumps

	public static class Eq extends Branch {

		public final boolean pos;
		public final int rk_left;
		public final int rk_right;

		public Eq(Target trueBranch, Target falseBranch, int a, int b, int c) {
			super(trueBranch, falseBranch);
			this.pos = (a == 0);
			this.rk_left = registerOrConst(b);
			this.rk_right = registerOrConst(c);
		}

		@Override
		public String toString() {
			return (pos ? "EQ" : "NOT-EQ") +  "(" + rk_left + "," + rk_right + ")";
		}

	}

	public static class Lt extends Branch {

		public final boolean pos;
		public final int rk_left;
		public final int rk_right;

		public Lt(Target trueBranch, Target falseBranch, int a, int b, int c) {
			super(trueBranch, falseBranch);
			this.pos = (a == 0);
			this.rk_left = registerOrConst(b);
			this.rk_right = registerOrConst(c);
		}

		@Override
		public String toString() {
			return (pos ? "LT" : "NOT-LT") + "(" + rk_left + "," + rk_right + ")";
		}

	}

	public static class Le extends Branch {

		public final boolean pos;
		public final int rk_left;
		public final int rk_right;

		public Le(Target trueBranch, Target falseBranch, int a, int b, int c) {
			super(trueBranch, falseBranch);
			this.pos = (a == 0);
			this.rk_left = registerOrConst(b);
			this.rk_right = registerOrConst(c);
		}

		@Override
		public String toString() {
			return (pos ? "LE" : "NOT-LE") + "(" + rk_left + "," + rk_right + ")";
		}

	}

	public static class Test extends Branch {
		public final int r_index;
		public final boolean value;

		// coerce register #a to a boolean and compare to c

		public Test(Target trueBranch, Target falseBranch, int a, int c) {
			super(trueBranch, falseBranch);
			this.r_index = a;
			this.value = (c != 0);
		}

		@Override
		public String toString() {
			String suffix;

			SlotType tpe = inSlots().getType(r_index);

			switch (tpe) {
				case BOOLEAN:  suffix = "_B"; break;  // simple boolean comparison, do branch
				case ANY:      suffix = "_coerce"; break;  // coerce, compare, do branch
				case NIL:      suffix = "_false"; break;  // automatically false
				default:       suffix = "_true"; break;  // automatically true
			}

			return "TEST" + suffix + "(" + r_index + "," + value + ")";
		}

		@Override
		public Boolean canBeInlined() {
			SlotType tpe = inSlots().getType(r_index);

			switch (tpe) {
				case BOOLEAN:
				case ANY:
					return null;

				case NIL:
					return Boolean.FALSE;

				default:
					return Boolean.TRUE;
			}
		}

	}

	// TODO
	// FIXME: this changes the way we think about value propagation!
	/*	A B C	if (R(B) <=> C) then R(A) := R(B) else pc++	*/
	public static class TestSet extends Branch {

		public final int a;
		public final int b;
		public final int c;

		public TestSet(Target trueBranch, Target falseBranch, int a, int b, int c) {
			super(trueBranch, falseBranch);
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "TESTSET(" + a + "," + "," + b + "," + c + ")";
		}

	}

	public static class Call extends Linear {

		public final int r_tgt;
		public final int b;
		public final int c;

		public Call(int a, int b, int c) {
			this.r_tgt = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			String suffix = inSlots().getType(r_tgt) == SlotType.FUNCTION ? "_F" : "_mt";
			suffix += "_" + argTypesFromSlots(inSlots(), r_tgt + 1, b);
			suffix += c > 0 ? "_" + (c - 1) : "_var";

			return "CALL" + suffix + "(" + r_tgt + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots s) {

			if (b == 0 && !s.hasVarargs()) {
				throw new IllegalStateException("varargs expected on stack");
			}

			// Since we don't know what the called function does, we must
			// assume that it may change any open upvalue.
			for (int i = 0; i < s.fixedSize(); i++) {
				if (s.getState(i).isCaptured()) {
					s = s.updateType(i, SlotType.ANY);
				}
			}

			if (c > 0) {
				s = s.consumeVarargs();

				// (c - 1) is the exact number of result values
				for (int i = 0; i < c - 1; i++) {
					s = s.updateType(r_tgt + i, SlotType.ANY);
				}
			}
			else {
				// TODO: upvalues must be closed here
				s = s.setVarargs(r_tgt);
			}

			return s;
		}

	}

	public static class TailCall extends Exit {

		public final int r_tgt;
		public final int b;

		public TailCall(int a, int b) {
			this.r_tgt = a;
			this.b = b;
		}

		@Override
		public String toString() {
			String suffix = inSlots().getType(r_tgt) == SlotType.FUNCTION ? "_F" : "_mt";
			suffix += "_" + argTypesFromSlots(inSlots(), r_tgt + 1, b);

			return "TAILCALL" + suffix + "(" + r_tgt + "," + b + ")";
		}

		@Override
		public ReturnType returnType() {
			SlotType targetType = inSlots().getType(r_tgt);
			ArgTypes argTypes = argTypesFromSlots(inSlots(), r_tgt + 1, b);
			return new ReturnType.TailCallReturnType(targetType, argTypes);
		}

	}


	public static class Return extends Exit {

		public final int r_from;
		public final int b;

		public Return(int a, int b) {
			this.r_from = a;
			this.b = b;
		}

		@Override
		public String toString() {
			String suffix = b > 0 ? "_fix" : "_var";
			return "RETURN" + suffix + "(" + r_from + "," + b + ")";
		}

		@Override
		public ReturnType returnType() {
			return new ReturnType.ConcreteReturnType(argTypesFromSlots(inSlots(), r_from, b));
		}

	}

	public static class ForLoop extends Branch {

		public final int r_base;

		public ForLoop(Target trueBranch, Target falseBranch, int a) {
			super(trueBranch, falseBranch);
			this.r_base = a;
		}

		@Override
		public String toString() {
			return "FORLOOP(" + r_base + ")";
		}

		// TODO: updates the register (r_base + 0), and in the true branch copies (r_base + 0) to (r_base + 3)

		// TODO: can also be specialised

	}

	public static class ForPrep extends Linear {

		public final int r_base;

		public ForPrep(int a) {
			this.r_base = a;
		}

		@Override
		public String toString() {
			return "FORPREP" + loopType(inSlots()).toSuffix() + "(" + r_base + ")";
		}

		private NumOpType loopType(Slots s) {
			SlotType a0 = s.getType(r_base + 0);
			SlotType a1 = s.getType(r_base + 1);
			SlotType a2 = s.getType(r_base + 2);

			if (a0 == SlotType.NUMBER_INTEGER
					&& a1 == SlotType.NUMBER_INTEGER
					&& a2 == SlotType.NUMBER_INTEGER) {

				return NumOpType.Integer;
			}
			else if (a0.isNumber()
					&& a1.isNumber()
					&& a2.isNumber()) {

				if (a0 == SlotType.NUMBER_FLOAT
						|| a1 == SlotType.NUMBER_FLOAT
						|| a2 == SlotType.NUMBER_FLOAT) {
					return NumOpType.Float;
				}
				else {
					return NumOpType.Number;
				}
			}
			else {
				// unable to determine at compile-time
				return NumOpType.Any;
			}
		}

		// TODO: do we convert all values in (r_base + 0) ... (r_base + 2) to the loop type?

		@Override
		protected Slots effect(Slots s) {
			SlotType tpe = loopType(s).toSlotType();
			return s.updateType(r_base + 3, tpe.isNumber()
					? tpe  // we know at compile-time that it's numeric
					: SlotType.NUMBER  // got something else -- may throw an exception at runtime!
			);
		}

	}

	// TODO: TFORCALL

	// TODO: TFORLOOP

	// TODO: SETLIST

	public static class Closure extends Linear implements LocalVariableEffect {

		public final int r_dest;
		public final Prototype prototype;

		public Closure(Prototype parent, int a, int bx) {
			this.r_dest = a;
			this.prototype = parent.getNestedPrototypes().get(bx);
		}

		@Override
		public String toString() {
			return "CLOSURE(" + r_dest + "," + PrototypePrinter.pseudoAddr(prototype) + ")";
		}

		@Override
		protected Slots effect(Slots s) {
			s = s.updateType(r_dest, SlotType.FUNCTION);

			for (Prototype.UpvalueDesc uvd : prototype.getUpValueDescriptions()) {
				if (uvd.inStack) {
					s = s.capture(uvd.index);
				}
			}

			return s;
		}

	}

	public static class Vararg extends Linear {

		public final int r_base;
		public final int b;

		public Vararg(int a, int b) {
			this.r_base = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "VARARG" + (b > 0 ? "_det" : "_indet") + "(" + r_base + "," + b + ")";
		}

		@Override
		public Slots effect(Slots s) {
			if (b > 0) {
				// (b - 1) is the number of values
				for (int i = 0; i < b - 1; i++) {
					s = s.updateType(r_base + i, SlotType.ANY);
				}
				return s;
			}
			else {
				return s.setVarargs(r_base);
			}
		}

	}

}
