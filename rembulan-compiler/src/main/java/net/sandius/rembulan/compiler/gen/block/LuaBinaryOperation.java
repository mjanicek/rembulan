package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Origin;
import net.sandius.rembulan.compiler.gen.PrototypeContext;
import net.sandius.rembulan.compiler.gen.Slot;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.util.Check;

import static net.sandius.rembulan.compiler.gen.block.StaticMathImplementation.MAY_BE_INTEGER;
import static net.sandius.rembulan.compiler.gen.block.StaticMathImplementation.MUST_BE_FLOAT;
import static net.sandius.rembulan.compiler.gen.block.StaticMathImplementation.MUST_BE_INTEGER;

public class LuaBinaryOperation extends Linear implements LuaInstruction {

	public enum Op {
		ADD, SUB, MUL, MOD, POW, DIV, IDIV, BAND, BOR, BXOR, SHL, SHR
	}

	public final PrototypeContext context;

	public final Op op;

	public final int r_dest;
	public final int rk_left;
	public final int rk_right;

	public LuaBinaryOperation(PrototypeContext context, Op op, int a, int b, int c) {
		this.context = Check.notNull(context);
		this.op = Check.notNull(op);
		this.r_dest = a;
		this.rk_left = LuaUtils.registerOrConst(b);
		this.rk_right = LuaUtils.registerOrConst(c);
	}

	public static StaticMathImplementation mathForOp(Op op) {
		switch (op) {
			case ADD:  return MAY_BE_INTEGER;
			case SUB:  return MAY_BE_INTEGER;
			case MUL:  return MAY_BE_INTEGER;
			case MOD:  return MAY_BE_INTEGER;
			case POW:  return MUST_BE_FLOAT;
			case DIV:  return MUST_BE_FLOAT;
			case IDIV: return MAY_BE_INTEGER;
			case BAND: return MUST_BE_INTEGER;
			case BOR:  return MUST_BE_INTEGER;
			case BXOR: return MUST_BE_INTEGER;
			case SHL:  return MUST_BE_INTEGER;
			case SHR:  return MUST_BE_INTEGER;
			default:   throw new IllegalArgumentException("Illegal operation: " + op);
		}
	}
	
	@Override
	public String toString() {
		return op.name() + opType(inSlots()).toSuffix() + "(" + r_dest + "," + rk_left + "," + rk_right + ")";
	}

	protected static Type slotType(PrototypeContext context, SlotState slots, int rk) {
		return rk < 0 ? context.constType(-rk - 1) : slots.typeAt(rk);
	}

	protected NumOpType opType(SlotState s) {
		return mathForOp(op).opType(slotType(context, s, rk_left), slotType(context, s, rk_right));
	}

	@Override
	protected SlotState effect(SlotState s) {
		return s.update(r_dest, Slot.of(Origin.Computed.in(this), opType(s).toSlotType()));
	}

	@Override
	public void emit(CodeEmitter e) {
		switch (op) {
			case ADD:  e.codeVisitor().visitAdd(this, inSlots(), r_dest, rk_left, rk_right); break;
			case SUB:  e.codeVisitor().visitSub(this, inSlots(), r_dest, rk_left, rk_right); break;
			case MUL:  e.codeVisitor().visitMul(this, inSlots(), r_dest, rk_left, rk_right); break;
			case MOD:  e.codeVisitor().visitMod(this, inSlots(), r_dest, rk_left, rk_right); break;
			case POW:  e.codeVisitor().visitPow(this, inSlots(), r_dest, rk_left, rk_right); break;
			case DIV:  e.codeVisitor().visitDiv(this, inSlots(), r_dest, rk_left, rk_right); break;
			case IDIV: e.codeVisitor().visitIDiv(this, inSlots(), r_dest, rk_left, rk_right); break;
			case BAND: e.codeVisitor().visitBAnd(this, inSlots(), r_dest, rk_left, rk_right); break;
			case BOR:  e.codeVisitor().visitBOr(this, inSlots(), r_dest, rk_left, rk_right); break;
			case BXOR: e.codeVisitor().visitBXOr(this, inSlots(), r_dest, rk_left, rk_right); break;
			case SHL:  e.codeVisitor().visitShl(this, inSlots(), r_dest, rk_left, rk_right); break;
			case SHR:  e.codeVisitor().visitShr(this, inSlots(), r_dest, rk_left, rk_right); break;
		}
	}

}
