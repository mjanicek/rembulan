package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.CodeVisitor;
import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.gen.Origin;
import net.sandius.rembulan.compiler.gen.PrototypeContext;
import net.sandius.rembulan.compiler.gen.ReturnType;
import net.sandius.rembulan.compiler.gen.Slot;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.types.FunctionType;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.compiler.types.TypeSeq;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;

import static net.sandius.rembulan.compiler.gen.block.LuaUtils.argTypesFromSlots;
import static net.sandius.rembulan.compiler.gen.block.LuaUtils.registerOrConst;

public interface LuaInstruction {

	enum NumOpType {
		Integer,
		Float,
		Number,
		Any;

		public Type toSlotType() {
			switch (this) {
				case Integer:  return LuaTypes.NUMBER_INTEGER;
				case Float:    return LuaTypes.NUMBER_FLOAT;
				case Number:   return LuaTypes.NUMBER;
				case Any:
				default:       return LuaTypes.ANY;
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


	class Move extends Linear implements LuaInstruction {

		public final int r_dest;
		public final int r_src;

		public Move(int a, int b) {
			this.r_dest = a;
			this.r_src = b;
		}

		@Override
		public String toString() {
			boolean nop = inSlots().slotAt(r_src).equals(inSlots().slotAt(r_dest));
			return "MOVE" + (nop ? "_nop" : "") + "(" + r_dest + "," + r_src + ")";
		}

		@Override
		protected SlotState effect(SlotState s) {
			return s.update(r_dest, s.slotAt(r_src));
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitMove(this, inSlots(), r_src, r_dest);
		}

	}

	class LoadK extends Linear implements LuaInstruction {

		public final PrototypeContext context;

		public final int r_dest;
		public final int constIndex;

		public LoadK(PrototypeContext context, int a, int bx) {
			this.context = Check.notNull(context);
			this.r_dest = a;
			this.constIndex = bx;
		}

		@Override
		public String toString() {
			return "LOADK(" + r_dest + "," + constIndex + ")";
		}

		@Override
		protected SlotState effect(SlotState s) {
			return s.update(r_dest, new Origin.Constant(constIndex), context.constType(constIndex));
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitLoadK(this, inSlots(), r_dest, constIndex);
		}

	}

	class LoadBool extends Linear implements LuaInstruction {

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
		protected SlotState effect(SlotState s) {
			return s.update(r_dest, Slot.of(Origin.BooleanConstant.fromBoolean(value), LuaTypes.BOOLEAN));
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitLoadBool(this, inSlots(), r_dest, value);
		}

	}

	class LoadNil extends Linear implements LuaInstruction {

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
		protected SlotState effect(SlotState s) {
			for (int i = 0; i < count; i++) {
				s = s.update(r_dest + i, Slot.NIL_SLOT);
			}
			return s;
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitLoadNil(this, inSlots(), r_dest, count);
		}

	}

	class GetUpVal extends Linear implements LuaInstruction {

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
		protected SlotState effect(SlotState s) {
			return s.update(r_dest, Slot.of(new Origin.Upvalue(upvalueIndex), LuaTypes.ANY));
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitGetUpVal(this, inSlots(), r_dest, upvalueIndex);
		}

	}

	class GetTabUp extends Linear implements LuaInstruction {

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
		protected SlotState effect(SlotState s) {
			return s.update(r_dest, Slot.of(Origin.Computed.in(this), LuaTypes.ANY));
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitGetTabUp(this, inSlots(), r_dest, upvalueIndex, rk_key);
		}

	}

	class GetTable extends Linear implements LuaInstruction {

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
			String suffix = inSlots().typeAt(r_tab).isSubtypeOf(LuaTypes.TABLE) ? "_T" : "";
			return "GETTABLE" + suffix + "(" + r_dest + "," + r_tab + "," + rk_key + ")";
		}

		@Override
		protected SlotState effect(SlotState s) {
			return s.update(r_dest, Slot.of(Origin.Computed.in(this), LuaTypes.ANY));
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitGetTable(this, inSlots(), r_dest, r_tab, rk_key);
		}

	}

	class SetTabUp extends Linear implements LuaInstruction {

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

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitSetTabUp(this, inSlots(), upvalueIndex, rk_key, rk_value);
		}

	}

	class SetUpVal extends Linear implements LuaInstruction {

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

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitSetUpVal(this, inSlots(), r_src, upvalueIndex);
		}

	}

	class SetTable extends Linear implements LuaInstruction {

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
			String suffix = (inSlots().typeAt(r_tab).isSubtypeOf(LuaTypes.TABLE) ? "_T" : "");
			return "SETTABLE" + suffix + "(" + r_tab + "," + rk_key + "," + rk_value + ")";
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitSetTable(this, inSlots(), r_tab, rk_key, rk_value);
		}

	}

	class NewTable extends Linear implements LuaInstruction {

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
		protected SlotState effect(SlotState s) {
			return s.update(r_dest, Slot.of(Origin.Computed.in(this), LuaTypes.TABLE));
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitNewTable(this, inSlots(), r_dest, arraySize, hashSize);
		}

	}

	class Self extends Linear implements LuaInstruction {

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
			String suffix = inSlots().typeAt(r_self).isSubtypeOf(LuaTypes.TABLE) ? "_T" : "";
			return "SELF" + suffix + "(" + r_dest + "," + r_self + "," + rk_key + ")";
		}

		@Override
		protected SlotState effect(SlotState s) {
			return s.update(r_dest + 1, s.slotAt(r_self))
					.update(r_dest, Slot.of(Origin.Computed.in(this), LuaTypes.ANY));
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitSelf(this, inSlots(), r_dest, r_self, rk_key);
		}

	}

	class Concat extends Linear implements LuaInstruction {

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

		private boolean allStringable(SlotState s) {
			for (int i = r_begin; i <= r_end; i++) {
				Type tpe = s.typeAt(i);
				if (!(tpe.isSubtypeOf(LuaTypes.STRING) || tpe.isSubtypeOf(LuaTypes.NUMBER))) {
					return false;
				}
			}
			return true;
		}

		@Override
		protected SlotState effect(SlotState s) {
			return s.update(r_dest, Slot.of(Origin.Computed.in(this), allStringable(s) ? LuaTypes.STRING : LuaTypes.ANY));
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitConcat(this, inSlots(), r_dest, r_begin, r_end);
		}

	}

	// No explicit node for jumps

	class Eq extends Branch implements LuaInstruction {

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

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitEq(this, inSlots(), pos, rk_left, rk_right, trueBranch(), falseBranch());
		}

	}

	class Lt extends Branch implements LuaInstruction {

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

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitLt(this, inSlots(), pos, rk_left, rk_right, trueBranch(), falseBranch());
		}

	}

	class Le extends Branch implements LuaInstruction {

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

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitLe(this, inSlots(), pos, rk_left, rk_right, trueBranch(), falseBranch());
		}

	}

	/*	A C	if not (R(A) <=> C) then pc++			*/
	class Test extends Branch implements LuaInstruction {
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
			Type tpe = inSlots().typeAt(r_index);

			String suffix = (tpe.isSubtypeOf(LuaTypes.BOOLEAN)
					? "_B"  // simple boolean comparison, do branch
					: (tpe.equals(LuaTypes.ANY)
							? "_coerce"  // coerce, compare, do branch
							: (tpe.equals(LuaTypes.NIL) && value
									? "_false"  // automatically false
									: "_true"  // automatically true
							)
					)
			);

			return "TEST" + suffix + "(" + r_index + "," + value + ")";
		}

		@Override
		public InlineTarget canBeInlined() {
			Type tpe = inSlots().typeAt(r_index);

			if (tpe.equals(LuaTypes.BOOLEAN) || tpe.equals(LuaTypes.ANY) || tpe.equals(LuaTypes.DYNAMIC)) return InlineTarget.CANNOT_BE_INLINED;
			else if (tpe.equals(LuaTypes.NIL)) return value ? InlineTarget.FALSE_BRANCH : InlineTarget.TRUE_BRANCH;
			else return value ? InlineTarget.TRUE_BRANCH : InlineTarget.FALSE_BRANCH;
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitTest(this, inSlots(), r_index, value, trueBranch(), falseBranch());
		}

	}

	// TODO
	// FIXME: this changes the way we think about value propagation!
	// Not used: translated as a TEST followed by MOVE in the true branch
	/*	A B C	if (R(B) <=> C) then R(A) := R(B) else pc++	*/
/*
	class TestSet extends Branch implements LuaInstruction {

		public final int r_set;
		public final int r_test;
		public final boolean value;

		public TestSet(Target trueBranch, Target falseBranch, int a, int b, int c) {
			super(trueBranch, falseBranch);
			this.r_set = a;
			this.r_test = b;
			this.value = (c != 0);
		}

		@Override
		public String toString() {
			Type tpe = inSlots().getType(r_test);

			String suffix = (tpe == Type.BOOLEAN
					? "_B"  // simple boolean comparison, do branch
					: (tpe == Type.ANY
							? "_coerce"  // coerce, compare, do branch
							: (tpe == Type.NIL
									? "_false"  // automatically false
									: "_true"  // automatically true
							)
					)
			);

			return "TESTSET" + suffix + "(" + r_set + "," + r_test + "," + value + ")";
		}

		@Override
		public InlineTarget canBeInlined() {
			Type tpe = inSlots().getType(r_test);

			if (tpe == Type.BOOLEAN || tpe == Type.ANY) return InlineTarget.CANNOT_BE_INLINED;
			else if (tpe == Type.NIL) return InlineTarget.FALSE_BRANCH;
			else return InlineTarget.TRUE_BRANCH;
		}

		// TODO: effect on the true branch

	}
*/

	interface CallInstruction extends LuaInstruction {

		Slot callTarget();

		TypeSeq callArguments();

	}

	class Call extends Linear implements LuaInstruction, CallInstruction {

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
			String suffix = callTarget().type() instanceof FunctionType ? "_F" : "_mt";
			suffix += "_" + callArguments();
			suffix += c > 0 ? "_" + (c - 1) : "_var";

			return "CALL" + suffix + "(" + r_tgt + "," + b + "," + c + ")";
		}

		@Override
		protected SlotState effect(SlotState s) {

			if (b == 0 && !s.hasVarargs()) {
				throw new IllegalStateException("varargs expected on stack");
			}

			// Since we don't know what the called function does, we must
			// assume that it may change any open upvalue.
			for (int i = 0; i < s.fixedSize(); i++) {
				if (s.isCaptured(i)) {
					s = s.update(i, Slot.of(Origin.Computed.in(this), LuaTypes.ANY));
				}
			}

			Type targetType = s.typeAt(r_tgt);
			TypeSeq retType = targetType instanceof FunctionType ? ((FunctionType) targetType).returnTypes() : TypeSeq.vararg();

			if (c > 0) {
				s = s.consumeVarargs();

				// (c - 1) is the exact number of result values
				for (int i = 0; i < c - 1; i++) {
					s = s.update(r_tgt + i, new Origin.CallResult(this, i), retType.get(i));
				}
			}
			else {
				// TODO: upvalues must be closed here
				s = s.setVarargs(r_tgt);
			}

			return s;
		}

		@Override
		public Slot callTarget() {
			return inSlots().slotAt(r_tgt);
		}

		@Override
		public TypeSeq callArguments() {
			return argTypesFromSlots(inSlots(), r_tgt + 1, b);
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitCall(this, inSlots(), r_tgt, b, c);
		}

	}

	class TailCall extends Exit implements LuaInstruction, CallInstruction {

		public final int r_tgt;
		public final int b;

		public TailCall(int a, int b) {
			this.r_tgt = a;
			this.b = b;
		}

		@Override
		public String toString() {
			String suffix = inSlots().typeAt(r_tgt) instanceof FunctionType ? "_F" : "_mt";
			suffix += "_" + argTypesFromSlots(inSlots(), r_tgt + 1, b);

			return "TAILCALL" + suffix + "(" + r_tgt + "," + b + ")";
		}

		@Override
		public ReturnType returnType() {
			Type targetType = callTarget().type();
			TypeSeq typeSeq = callArguments();
			return new ReturnType.TailCallReturnType(targetType, typeSeq);
		}

		@Override
		public Slot callTarget() {
			return inSlots().slotAt(r_tgt);
		}

		@Override
		public TypeSeq callArguments() {
			return argTypesFromSlots(inSlots(), r_tgt + 1, b);
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitTailCall(this, inSlots(), r_tgt, b);
		}

	}


	class Return extends Exit implements LuaInstruction {

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

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitReturn(this, inSlots(), r_from, b);
		}

	}

	class ForLoop extends Branch implements LuaInstruction {

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

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitForLoop(this, inSlots(), r_base);
		}

	}

	class ForPrep extends Linear implements LuaInstruction {

		public final int r_base;

		public ForPrep(int a) {
			this.r_base = a;
		}

		@Override
		public String toString() {
			return "FORPREP" + loopType(inSlots()).toSuffix() + "(" + r_base + ")";
		}

		private NumOpType loopType(SlotState s) {
			Type a0 = s.typeAt(r_base + 0);
			Type a1 = s.typeAt(r_base + 1);
			Type a2 = s.typeAt(r_base + 2);

			if (a0.isSubtypeOf(LuaTypes.NUMBER_INTEGER)
					&& a1.isSubtypeOf(LuaTypes.NUMBER_INTEGER)
					&& a2.isSubtypeOf(LuaTypes.NUMBER_INTEGER)) {

				return NumOpType.Integer;
			}
			else if (a0.isSubtypeOf(LuaTypes.NUMBER)
					&& a1.isSubtypeOf(LuaTypes.NUMBER)
					&& a2.isSubtypeOf(LuaTypes.NUMBER)) {

				if (a0.isSubtypeOf(LuaTypes.NUMBER_FLOAT)
						|| a1.isSubtypeOf(LuaTypes.NUMBER_FLOAT)
						|| a2.isSubtypeOf(LuaTypes.NUMBER_FLOAT)) {
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
		protected SlotState effect(SlotState s) {
			Type tpe = loopType(s).toSlotType();

			return s.update(r_base + 3, Slot.of(Origin.Computed.in(this), tpe.isSubtypeOf(LuaTypes.NUMBER)
					? tpe  // we know at compile-time that it's numeric
					: LuaTypes.NUMBER  // got something else -- may throw an exception at runtime!
			));
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitForPrep(this, inSlots(), r_base);
		}

	}

	// TODO: TFORCALL

	// TODO: TFORLOOP

	// TODO: SETLIST

	class Closure extends Linear implements LuaInstruction, LocalVariableEffect {

		public final PrototypeContext context;

		public final int r_dest;
		public final int index;

		public Closure(PrototypeContext context, int a, int bx) {
			this.context = Check.notNull(context);
			this.r_dest = a;
			this.index = bx;
		}

		@Override
		public String toString() {
			return "CLOSURE(" + r_dest + "," + context.nestedPrototypeName(index) + ")";
		}

		@Override
		protected SlotState effect(SlotState s) {
			FunctionType tpe = context.nestedPrototypeType(index);

			s = s.update(r_dest, new Origin.Closure(index), tpe);

			for (Prototype.UpvalueDesc uvd : context.nestedPrototype(index).getUpValueDescriptions()) {
				if (uvd.inStack) {
					s = s.capture(uvd.index);
				}
			}

			return s;
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitClosure(this, inSlots(), r_dest, index);
		}

	}

	class Vararg extends Linear implements LuaInstruction {

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
		public SlotState effect(SlotState s) {
			if (b > 0) {
				// (b - 1) is the number of values
				for (int i = 0; i < b - 1; i++) {
					s = s.update(r_base + i, new Origin.Vararg(i), LuaTypes.ANY);
				}
				return s;
			}
			else {
				return s.setVarargs(r_base);
			}
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitVararg(this, inSlots(), r_base, b);
		}

	}

}
