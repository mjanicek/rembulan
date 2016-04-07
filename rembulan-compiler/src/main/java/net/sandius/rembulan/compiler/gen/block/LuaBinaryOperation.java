package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.LuaTypes;
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

	protected Type slotType(SlotState s, int idx) {
		return idx < 0 ? context.constType(-idx - 1) : s.typeAt(idx);
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

	@Override
	public boolean needsResumePoint() {
		switch (opType(inSlots())) {
			case Any: return true;
			default: return false;
		}
	}

	protected abstract String methodPrefix();

	@Override
	public void emit(CodeEmitter e) {
		SlotState s = inSlots();

		NumOpType ot = opType(slotType(s, rk_left), slotType(s, rk_right));

		switch (ot) {
			case Integer:
				e._load_reg_or_const(rk_left, s, Number.class);
				e._load_reg_or_const(rk_right, s, Number.class);
				e._dispatch_binop(methodPrefix() + "_integer", Number.class);
				e._store(r_dest, s);
				break;

			case Float:
				e._load_reg_or_const(rk_left, s, Number.class);
				e._load_reg_or_const(rk_right, s, Number.class);
				e._dispatch_binop(methodPrefix() + "_float", Number.class);
				e._store(r_dest, s);
				break;

			case Number:
				e._load_reg_or_const(rk_left, s, Number.class);
				e._load_reg_or_const(rk_right, s, Number.class);
				e._dispatch_binop(methodPrefix(), Number.class);
				e._store(r_dest, s);
				break;

			case Any:
				e._save_pc(this);

				e._loadState();
				e._loadObjectSink();
				e._load_reg_or_const(rk_left, s, Object.class);
				e._load_reg_or_const(rk_right, s, Object.class);
				e._dispatch_generic_mt_2(methodPrefix());

				e._resumptionPoint(this);
				e._retrieve_0();
				e._store(r_dest, s);
				break;
		}
	}

	public static class Add extends LuaBinaryOperation {

		public Add(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "ADD";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mayBeInteger(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "add";
		}

	}

	public static class Sub extends LuaBinaryOperation {

		public Sub(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "SUB";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mayBeInteger(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "sub";
		}

	}

	public static class Mul extends LuaBinaryOperation {

		public Mul(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "MUL";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mayBeInteger(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "mul";
		}

	}

	public static class Mod extends LuaBinaryOperation {

		public Mod(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "MOD";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mayBeInteger(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "mod";
		}

	}

	public static class Pow extends LuaBinaryOperation {

		public Pow(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "POW";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeFloat(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "pow";
		}

	}

	public static class Div extends LuaBinaryOperation {

		public Div(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "DIV";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeFloat(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "div";
		}

	}

	public static class IDiv extends LuaBinaryOperation {

		public IDiv(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "IDIV";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mayBeInteger(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "idiv";
		}

	}

	public static class BAnd extends LuaBinaryOperation {

		public BAnd(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "BAND";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeInteger(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "band";
		}

	}

	public static class BOr extends LuaBinaryOperation {

		public BOr(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "BOR";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeInteger(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "bor";
		}

	}

	public static class BXor extends LuaBinaryOperation {

		public BXor(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "BXOR";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeInteger(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "bxor";
		}

	}

	public static class Shl extends LuaBinaryOperation {

		public Shl(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "SHL";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeInteger(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "shl";
		}

	}

	public static class Shr extends LuaBinaryOperation {

		public Shr(PrototypeContext context, int dest, int b, int c) {
			super(context, dest, b, c);
		}

		@Override
		protected String name() {
			return "SHR";
		}

		@Override
		protected NumOpType opType(Type l, Type r) {
			return mustBeInteger(l, r);
		}

		@Override
		protected String methodPrefix() {
			return "shr";
		}

	}

}
