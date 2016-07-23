package net.sandius.rembulan.lbc.recompiler.gen.block;

import net.sandius.rembulan.compiler.analysis.types.LuaTypes;
import net.sandius.rembulan.compiler.analysis.types.Type;
import net.sandius.rembulan.lbc.recompiler.gen.CodeVisitor;
import net.sandius.rembulan.lbc.recompiler.gen.Origin;
import net.sandius.rembulan.lbc.recompiler.gen.Slot;
import net.sandius.rembulan.lbc.recompiler.gen.SlotState;
import net.sandius.rembulan.util.Check;

public class LuaUnaryOperation extends Linear implements LuaInstruction {

	public enum Op {
		UNM, BNOT, NOT, LEN
	}

	public final Op op;

	public final int r_dest;
	public final int r_arg;

	public LuaUnaryOperation(Op op, int a, int b) {
		this.op = Check.notNull(op);
		this.r_dest = a;
		this.r_arg = b;
	}

	protected Type resultType(Type in) {
		switch (op) {
			case UNM:  return in.isSubtypeOf(LuaTypes.NUMBER) ? in : LuaTypes.ANY;

			// TODO: for constants, we could determine whether the argument is coercible to integer -> need access to it
			case BNOT: return in.isSubtypeOf(LuaTypes.NUMBER_INTEGER) ? LuaTypes.NUMBER_INTEGER : LuaTypes.ANY;
			case NOT:  return LuaTypes.BOOLEAN;
			case LEN:  return in.isSubtypeOf(LuaTypes.STRING) ? LuaTypes.NUMBER_INTEGER : LuaTypes.ANY;
			default: throw new IllegalStateException("Illegal op: " + op);
		}
	}

	@Override
	public String toString() {
		Type rt = resultType(inSlots().typeAt(r_arg));
		String suffix = rt != LuaTypes.ANY ? "_" + rt : "";
		return op.name() + suffix + "(" + r_dest + "," + r_arg + ")";
	}

	@Override
	protected SlotState effect(SlotState s) {
		return s.update(r_dest, Slot.of(Origin.Computed.in(this), resultType(s.typeAt(r_arg))));
	}

	@Override
	public void emit(CodeVisitor visitor) {
		switch (op) {
			case UNM:  visitor.visitUnm(this, inSlots(), r_dest, r_arg); break;
			case BNOT: visitor.visitBNot(this, inSlots(), r_dest, r_arg); break;
			case NOT:  visitor.visitNot(this, inSlots(), r_dest, r_arg); break;
			case LEN:  visitor.visitLen(this, inSlots(), r_dest, r_arg); break;
		}
	}

}
