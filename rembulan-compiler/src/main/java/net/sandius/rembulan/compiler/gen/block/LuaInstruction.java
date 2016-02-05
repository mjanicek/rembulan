package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.ArgTypes;
import net.sandius.rembulan.compiler.gen.ReturnType;
import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.compiler.gen.Slots.SlotType;
import net.sandius.rembulan.lbc.OpCode;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
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

	public static class Move extends Linear {
		public final int dest;
		public final int src;

		public Move(int dest, int src) {
			this.dest = dest;
			this.src = src;
		}

		@Override
		public String toString() {
			return "MOVE(" + dest + "," + src + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(dest, in.getType(src));
		}

	}

	public static class LoadK extends Linear {

		public final Prototype prototype;

		public final int dest;
		public final int kIndex;

		public LoadK(Prototype prototype, int dest, int kIndex) {
			this.prototype = Objects.requireNonNull(prototype);
			this.dest = dest;
			this.kIndex = kIndex;
		}

		@Override
		public String toString() {
			return "LOADK(" + dest + "," + kIndex + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(dest, constantType(prototype.getConstants().get(kIndex)));
		}

	}

	public static class LoadNil extends Linear {
		public final int dest;
		public final int lastOffset;

		public LoadNil(int dest, int lastOffset) {
			this.dest = dest;
			this.lastOffset = lastOffset;
		}

		@Override
		public String toString() {
			return "LOADNIL(" + dest + "," + lastOffset + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			Slots s = in;
			for (int i = dest; i <= dest + lastOffset; i++) {
				s = s.updateType(i, SlotType.NIL);
			}
			return s;
		}

	}

	public static class LoadBool extends Linear {
		public final int dest;
		public final boolean value;

		public LoadBool(int dest, boolean value) {
			this.dest = dest;
			this.value = value;
		}

		@Override
		public String toString() {
			return "LOADBOOL(" + dest + "," + value + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(dest, SlotType.BOOLEAN);
		}
	}

	public enum BinOpType {
		ADD, SUB, MUL, MOD, POW, DIV, IDIV, BAND, BOR, BXOR, SHL, SHR;

		public static BinOpType fromOpcode(int opcode) {
			switch (opcode) {
				case OpCode.ADD:  return BinOpType.ADD;
				case OpCode.SUB:  return BinOpType.SUB;
				case OpCode.MUL:  return BinOpType.MUL;
				case OpCode.MOD:  return BinOpType.MOD;
				case OpCode.POW:  return BinOpType.POW;
				case OpCode.DIV:  return BinOpType.DIV;
				case OpCode.IDIV: return BinOpType.IDIV;
				case OpCode.BAND: return BinOpType.BAND;
				case OpCode.BOR:  return BinOpType.BOR;
				case OpCode.BXOR: return BinOpType.BXOR;
				case OpCode.SHL:  return BinOpType.SHL;
				case OpCode.SHR:  return BinOpType.SHR;
				default: return null;
			}
		}
	}

	public enum UnOpType {
		UNM, BNOT, NOT, LEN;

		public static UnOpType fromOpcode(int opcode) {
			switch (opcode) {
				case OpCode.UNM:  return UnOpType.UNM;
				case OpCode.BNOT: return UnOpType.BNOT;
				case OpCode.NOT:  return UnOpType.NOT;
				case OpCode.LEN:  return UnOpType.LEN;
				default: return null;
			}
		}
	}

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
		protected Slots effect(Slots in) {
			SlotType argType = b < 0 ? constantType(prototype.getConstants().get(-b - 1)) : in.getType(b);

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

			return in.updateType(dest, resultType);
		}
	}

	public static class Concat extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public Concat(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "CONCAT(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			throw new UnsupportedOperationException();  // TODO
		}

	}

	public static class Call extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public Call(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		private boolean onFuncObject(Slots s) {
			return s.getType(a) == SlotType.FUNCTION;
		}

		@Override
		public String toString() {
			String suffix = onFuncObject(inSlots()) ? "_F" : "_mt";
			suffix += "_" + argTypesFromSlots(inSlots(), a + 1, b);
			suffix += c > 0 ? "_" + (c - 1) : "_var";

			return "CALL" + suffix + "(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			Slots s = in;


			if (b > 0) {
				// (b - 1) is the exact number of arguments
				// TODO anything here?
			}
			else {
				if (!s.hasVarargs()) {
					throw new IllegalStateException("varargs expected on stack");
				}
			}

			// Since we don't know what the called function does, we must
			// assume that it may change any open upvalue.
			for (int i = 0; i < in.size(); i++) {
				// FIXME: re-add this!!!
//				if (s.getState(i).isCaptured()) {
//					s = s.updateType(i, SlotType.ANY);
//				}
			}

			if (c > 0) {
				s = s.consumeVarargs();
				// (c - 1) is the exact number of result values
				for (int i = a; i < a + c - 1; i++) {
					s = s.updateType(i, SlotType.ANY);
				}
			}
			else {
				// TODO: upvalues must be closed here
				s = s.setVarargs(a);
			}

			return s;
		}

	}

	public static class TailCall extends Exit {
		public final int a;
		public final int b;

		public TailCall(int a, int b, int c) {
			Check.isEq(c, 0);
			this.a = a;
			this.b = b;
		}

		private boolean onFuncObject(Slots s) {
			return s.getType(a) == SlotType.FUNCTION;
		}

		@Override
		public String toString() {
			String suffix = onFuncObject(inSlots()) ? "_F" : "_mt";
			suffix += "_" + argTypesFromSlots(inSlots(), a + 1, b);

			return "TAILCALL" + suffix + "(" + a + "," + b + ")";
		}

		@Override
		public ReturnType returnType() {
			SlotType targetType = inSlots().getType(a);
			ArgTypes argTypes = argTypesFromSlots(inSlots(), a + 1, b);
			return new ReturnType.TailCallReturnType(targetType, argTypes);
		}

	}


	public static class Return extends Exit {
		public final int a;
		public final int b;

		public Return(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			String suffix = b > 0 ? "_fix" : "_var";
			return "RETURN" + suffix + "(" + a + "," + b + ")";
		}

		@Override
		public ReturnType returnType() {
			return new ReturnType.ConcreteReturnType(argTypesFromSlots(inSlots(), a, b));
		}

	}

	public enum NumOpType {
		Integer,
		Float,
		Number,
		Any;

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

	public static class ForPrep extends Linear {
		public final int a;

		public ForPrep(int a) {
			this.a = a;
		}

		@Override
		public String toString() {
			return "FORPREP" + loopType(inSlots()).toSuffix() + "(" + a + ")";
		}

		private NumOpType loopType(Slots in) {
			SlotType a0 = in.getType(a + 0);
			SlotType a1 = in.getType(a + 1);
			SlotType a2 = in.getType(a + 2);

			if (a0 == SlotType.NUMBER_INTEGER
					&& a1 == SlotType.NUMBER_INTEGER
					&& a2 == SlotType.NUMBER_INTEGER) {

				return NumOpType.Integer;
			}
			else if (a0.isNumber()
					&& a1.isNumber()
					&& a2.isNumber()) {

				return NumOpType.Float;
			}
			else {
				// unable to determine at compile-time, we can nevertheless expect
				// this to be a numeric loop -- will throw an error otherwise
				return NumOpType.Number;
			}
		}

		@Override
		protected Slots effect(Slots in) {
			NumOpType tpe = loopType(in);
			switch (tpe) {
				case Integer: return in.updateType(a + 3, SlotType.NUMBER_INTEGER);
				case Float:   return in.updateType(a + 3, SlotType.NUMBER_FLOAT);
				default:      return in.updateType(a + 3, SlotType.NUMBER);
			}
		}

	}


	public static class GetUpVal extends Linear {
		public final int a;
		public final int b;

		public GetUpVal(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "GETUPVAL(" + a + "," + b + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(a, SlotType.ANY);
		}

	}

	public static class GetTabUp extends Linear {
		public final int a;
		public final int b;

		public GetTabUp(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "GETTABUP(" + a + "," + b + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(a, SlotType.ANY);
		}

	}

	public static class GetTable extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public GetTable(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "GETTABLE(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(a, SlotType.ANY);
		}

	}

	public static class SetTabUp extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public SetTabUp(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "SETTABUP(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			// TODO: this might have a possible effect?
			return in;
		}

	}

	public static class SetUpVal extends Linear {
		public final int a;
		public final int b;

		public SetUpVal(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "SETUPVAL(" + a + "," + b + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			// TODO: this might have a possible effect?
			return in;
		}

	}

	public static class SetTable extends Linear {
		public final int a;
		public final int b;
		public final int c;

		public SetTable(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "SETTABLE(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in;
		}

	}

	public static class NewTable extends Linear {
		public final int a;
		public final int b;
		public final int c;

		public NewTable(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "NEWTABLE(" + a + "," + b + "," + c + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			return in.updateType(a, SlotType.TABLE);
		}

	}

	public static class Eq extends Branch {
		public final boolean pos;
		public final int b;
		public final int c;

		public Eq(Target trueBranch, Target falseBranch, boolean pos, int b, int c) {
			super(trueBranch, falseBranch);
			this.pos = pos;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return (pos ? "EQ" : "NOT-EQ") +  "(" + b + "," + c + ")";
		}

	}

	public static class Lt extends Branch {
		public final boolean pos;
		public final int b;
		public final int c;

		public Lt(Target trueBranch, Target falseBranch, boolean pos, int b, int c) {
			super(trueBranch, falseBranch);
			this.pos = pos;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return (pos ? "LT" : "NOT-LT") + "(" + b + "," + c + ")";
		}

	}

	public static class Le extends Branch {
		public final boolean pos;
		public final int b;
		public final int c;

		public Le(Target trueBranch, Target falseBranch, boolean pos, int b, int c) {
			super(trueBranch, falseBranch);
			this.pos = pos;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return (pos ? "LE" : "NOT-LE") + "(" + b + "," + c + ")";
		}

	}

	public static class Test extends Branch {
		public final int a;
		public final int c;

		// coerce register #a to a boolean and compare to c

		public Test(Target trueBranch, Target falseBranch, int a, int c) {
			super(trueBranch, falseBranch);
			this.a = a;
			this.c = c;
		}

		@Override
		public String toString() {
			String suffix;

			SlotType tpe = inSlots().getType(a);

			switch (tpe) {
				case BOOLEAN:  suffix = "_B"; break;  // simple boolean comparison, do branch
				case ANY:      suffix = "_coerce"; break;  // coerce, compare, do branch
				case NIL:      suffix = "_false"; break;  // automatically false
				default:       suffix = "_true"; break;  // automatically true
			}

			return "TEST" + suffix + "(" + a + "," + (c != 0 ? "true" : "false") + ")";
		}

		@Override
		public Boolean canBeInlined() {
			SlotType tpe = inSlots().getType(a);

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

	public static class ForLoop extends Branch {
		public final int a;
		public final int sbx;

		public ForLoop(Target trueBranch, Target falseBranch, int a, int sbx) {
			super(trueBranch, falseBranch);
			this.a = a;
			this.sbx = sbx;
		}

		@Override
		public String toString() {
			return "FORLOOP(" + a + "," + sbx + ")";
		}

		// TODO: can also be specialised

	}

	public static class Closure extends Linear implements LocalVariableEffect {

		public final Prototype prototype;

		public final int dest;
		public final int index;

		public Closure(Prototype prototype, int dest, int index) {
			this.prototype = Objects.requireNonNull(prototype);
			this.dest = dest;
			this.index = index;
		}

		@Override
		public String toString() {
			return "CLOSURE(" + dest + "," + index + ")";
		}

		@Override
		protected Slots effect(Slots in) {
			Slots s = in.updateType(dest, SlotType.FUNCTION);

			Prototype p = prototype.getNestedPrototypes().get(index);
			for (Prototype.UpvalueDesc uvd : p.getUpValueDescriptions()) {
				if (uvd.inStack) {
					s = s.capture(uvd.index);
				}
			}

			return s;
		}

	}

	public static class Vararg extends Linear {
		public final int a;
		public final int b;

		public Vararg(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "VARARG" + (b > 0 ? "_det" : "_indet") + "(" + a + "," + b + ")";
		}

		@Override
		public Slots effect(Slots in) {
			Slots s = in;
			if (b > 0) {
				// (b - 1) is the number of values
				for (int i = 0; i < b - 1; i++) {
					s = s.updateType(a + i, SlotType.ANY);
				}
				return s;
			}
			else {
				return s.setVarargs(a);
			}
		}

	}

}
