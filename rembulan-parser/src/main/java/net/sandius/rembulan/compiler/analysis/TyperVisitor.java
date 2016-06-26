package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.BasicBlock;
import net.sandius.rembulan.compiler.Blocks;
import net.sandius.rembulan.compiler.BlocksVisitor;
import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.gen.block.StaticMathImplementation;
import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.compiler.types.Type;
import net.sandius.rembulan.util.Check;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static net.sandius.rembulan.compiler.gen.block.StaticMathImplementation.MAY_BE_INTEGER;
import static net.sandius.rembulan.compiler.gen.block.StaticMathImplementation.MUST_BE_FLOAT;
import static net.sandius.rembulan.compiler.gen.block.StaticMathImplementation.MUST_BE_INTEGER;

public class TyperVisitor extends BlocksVisitor {

	private final Map<Val, Type> valTypes;
	private final Map<PhiVal, Type> phiValTypes;
	private final Map<Label, VarState> varStates;

	private final Queue<Label> open;

	private boolean changed;
	private VarState currentVarState;

	public TyperVisitor() {
		this.valTypes = new HashMap<>();
		this.phiValTypes = new HashMap<>();
		this.varStates = new HashMap<>();
		this.open = new ArrayDeque<>();
	}

	public TypeInfo valTypes() {
		return TypeInfo.of(valTypes, phiValTypes);
	}

	private static Type joinTypes(Type a, Type b) {
		return a == null ? b : (b == null ? a : a.join(b));
	}

	private VarState currentVarState() {
		return currentVarState;
	}

	private class VarState {

		private final Map<Var, Type> types;

		private VarState(Map<Var, Type> types) {
			this.types = Check.notNull(types);
		}

		public VarState() {
			this(new HashMap<Var, Type>());
		}

		public VarState copy() {
			return new VarState(new HashMap<>(types));
		}

		public void store(Var v, Type t) {
			types.put(Check.notNull(v), Check.notNull(t));
		}

		public Type load(Var v) {
			Type t = types.get(Check.notNull(v));
			if (t == null) {
				throw new IllegalStateException(v + " used before stored into");
			}
			else {
				return t;
			}
		}

		public boolean joinWith(VarState that) {
			Check.notNull(that);

			Map<Var, Type> result = new HashMap<>();
			for (Var v : this.types.keySet()) {
				Type t = joinTypes(types.get(v), that.types.get(v));
				result.put(v, Check.notNull(t));
			}
			for (Var v : that.types.keySet()) {
				Type t = joinTypes(types.get(v), that.types.get(v));
				result.put(v, Check.notNull(t));
			}
			if (!result.equals(types)) {
				types.clear();
				types.putAll(result);
				return true;
			}
			else {
				return false;
			}
		}
	}

	private VarState varState(Label l) {
		VarState vs = varStates.get(l);
		if (vs == null) {
			VarState nvs = new VarState();
			varStates.put(l, nvs);
			return nvs;
		}
		else {
			return vs;
		}
	}

	private void assign(Val v, Type t) {
		Check.notNull(v);
		Check.notNull(t);

		Type ot = valTypes.put(v, t);
		if (t.equals(ot)) {
			// no change
		}
		else if (ot == null) {
			// initial assign
			changed = true;
		}
		else {
			changed = true;
//			throw new IllegalStateException(v + " assigned to more than once");
		}
	}

	private void assign(PhiVal pv, Type t) {
		Check.notNull(pv);
		Check.notNull(t);

		Type ot = phiValTypes.get(pv);
		if (ot != null) {
			Type nt = ot.join(t);
			if (!ot.equals(nt)) {
				phiValTypes.put(pv, nt);
				changed = true;
			}
			else {
				// no change
			}
		}
		else {
			// initial assign
			phiValTypes.put(pv, t);
			changed = true;
		}
	}

	private Type typeOf(Val v) {
		Type t = valTypes.get(v);
		if (t == null) {
			throw new IllegalStateException(v + " not assigned to yet");
		}
		else {
			return t;
		}
	}

	private Type typeOf(PhiVal pv) {
		Type t = phiValTypes.get(pv);
		if (t == null) {
			throw new IllegalStateException(pv + " not assigned to yet");
		}
		else {
			return t;
		}
	}

	@Override
	public void visit(IRFunc func) {
		Blocks blocks = func.blocks();

		VarState vs = varState(blocks.entryLabel());
		for (Var p : func.params()) {
			vs.store(p, LuaTypes.DYNAMIC);
		}

		visit(blocks);
	}

	@Override
	public void visit(Blocks blocks) {
		open.add(blocks.entryLabel());

		Map<Label, BasicBlock> index = blocks.index();

		while (!open.isEmpty()) {
			Label l = open.poll();
			BasicBlock b = index.get(l);

			changed = false;
			visit(b);
			if (changed) {
				for (Label nxt : b.end().nextLabels()) {
					open.add(nxt);
				}
			}
		}

	}

	@Override
	public void visit(BasicBlock block) {
		currentVarState = varState(block.label()).copy();

		changed = false;
		super.visit(block);

		for (Label nxt : block.end().nextLabels()) {
			VarState vs = varState(nxt);
			if (vs.joinWith(currentVarState)) {
				changed = true;
			}
		}

		if (changed) {
			for (Label nxt : block.end().nextLabels()) {
				open.add(nxt);
			}
		}

		currentVarState = null;
	}

	@Override
	public void visit(LoadConst.Nil node) {
		assign(node.dest(), LuaTypes.NIL);
	}

	@Override
	public void visit(LoadConst.Bool node) {
		assign(node.dest(), LuaTypes.BOOLEAN);
	}

	@Override
	public void visit(LoadConst.Int node) {
		assign(node.dest(), LuaTypes.NUMBER_INTEGER);

	}

	@Override
	public void visit(LoadConst.Flt node) {
		assign(node.dest(), LuaTypes.NUMBER_FLOAT);
	}

	@Override
	public void visit(LoadConst.Str node) {
		assign(node.dest(), LuaTypes.STRING);
	}

	private static StaticMathImplementation staticMath(BinOp.Op op) {
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
			default:   return null;
		}
	}

	private static boolean stringable(Type t) {
		return t.isSubtypeOf(LuaTypes.STRING) || t.isSubtypeOf(LuaTypes.NUMBER);
	}
	
	@Override
	public void visit(BinOp node) {
		Type l = typeOf(node.left());
		Type r = typeOf(node.right());

		final Type result;

		StaticMathImplementation math = staticMath(node.op());

		if (math != null) {
			result = math.opType(l, r).toSlotType();
		}
		else {
			switch (node.op()) {
				case CONCAT:
					result = stringable(l) && stringable(r) ? LuaTypes.STRING : LuaTypes.ANY;
					break;
				case EQ:
				case NEQ:
				case LT:
				case LE:
					result = LuaTypes.BOOLEAN;
					break;
				default: throw new UnsupportedOperationException("Illegal binary operation: " + node.op());
			}
		}

		assign(node.dest(), result);
	}

	@Override
	public void visit(UnOp node) {
		Type a = typeOf(node.arg());

		final Type result;
		switch (node.op()) {
			case UNM:  result = MAY_BE_INTEGER.opType(a).toSlotType(); break;
			case BNOT: result = MUST_BE_INTEGER.opType(a).toSlotType(); break;
			case NOT:  result = LuaTypes.BOOLEAN; break;
			case LEN:  result = a.isSubtypeOf(LuaTypes.STRING) ? LuaTypes.NUMBER_INTEGER : LuaTypes.ANY; break;
			default: throw new UnsupportedOperationException("Illegal unary operation: " + node.op());
		}

		assign(node.dest(), result);
	}

	@Override
	public void visit(TabNew node) {
		assign(node.dest(), LuaTypes.TABLE);
	}

	@Override
	public void visit(TabGet node) {
		assign(node.dest(), LuaTypes.ANY);
	}

	@Override
	public void visit(TabSet node) {
		// no effect on vals
	}

	@Override
	public void visit(TabStackAppend node) {
		// no effect on vals
	}

	@Override
	public void visit(VarLoad node) {
		Type t = currentVarState().load(node.var());
		assign(node.dest(), t);
	}

	@Override
	public void visit(VarStore node) {
		currentVarState().store(node.var(), typeOf(node.src()));
	}

	@Override
	public void visit(UpLoad node) {
		// TODO
		assign(node.dest(), LuaTypes.ANY);
	}

	@Override
	public void visit(UpStore node) {
		// no effect on vals
	}

	@Override
	public void visit(Vararg node) {
		// no effect on vals
	}

	@Override
	public void visit(Ret node) {
		// no effect on vals
	}

	@Override
	public void visit(TCall node) {
		// no effect on vals
	}

	@Override
	public void visit(Call node) {
		// TODO: assign type to the varargs
		// no effect on vals
	}

	@Override
	public void visit(StackGet node) {
		// TODO
		assign(node.dest(), LuaTypes.ANY);
	}

	@Override
	public void visit(PhiStore node) {
		assign(node.dest(), typeOf(node.src()));
	}

	@Override
	public void visit(PhiLoad node) {
		assign(node.dest(), typeOf(node.src()));
	}

	@Override
	public void visit(Label node) {
		// no effect on vals
	}

	@Override
	public void visit(Jmp node) {
		// no effect on vals
	}

	@Override
	public void visit(Closure node) {
		assign(node.dest(), LuaTypes.FUNCTION);  // FIXME
	}

	@Override
	public void visit(ToNumber node) {
		Type t = typeOf(node.src());
		final Type result;

		if (t.isSubtypeOf(LuaTypes.NUMBER)) {
			result = t;
		}
		else {
			result = LuaTypes.NUMBER;
		}

		assign(node.dest(), result);
	}

	@Override
	public void visit(ToNext node) {
		// no effect on vals
	}

	@Override
	public void visit(Branch branch) {
		// no effect on vals
	}

	@Override
	public void visit(Branch.Condition.Nil cond) {
		// no effect on vals
	}

	@Override
	public void visit(Branch.Condition.Bool cond) {
		// no effect on vals
	}

	@Override
	public void visit(Branch.Condition.NumLoopEnd cond) {
		// no effect on vals
	}

}
