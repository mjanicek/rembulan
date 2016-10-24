/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.analysis.types.FunctionType;
import net.sandius.rembulan.compiler.analysis.types.LuaTypes;
import net.sandius.rembulan.compiler.analysis.types.ReturnType;
import net.sandius.rembulan.compiler.analysis.types.Type;
import net.sandius.rembulan.compiler.analysis.types.TypeSeq;
import net.sandius.rembulan.compiler.ir.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import static net.sandius.rembulan.compiler.analysis.StaticMathImplementation.MAY_BE_INTEGER;
import static net.sandius.rembulan.compiler.analysis.StaticMathImplementation.MUST_BE_FLOAT;
import static net.sandius.rembulan.compiler.analysis.StaticMathImplementation.MUST_BE_INTEGER;

class TyperVisitor extends CodeVisitor {

	private final Map<Val, Type> valTypes;
	private final Map<PhiVal, Type> phiValTypes;
	private final Map<MultiVal, TypeSeq> multiValTypes;
	private final Map<Label, VarState> varStates;

	private final Set<Var> allVars;
	private final Set<Var> reifiedVars;

	private final Set<Label> seen;
	private final Queue<Label> open;

	private final Set<ReturnType> returnTypes;

	private boolean changed;
	private VarState currentVarState;

	public TyperVisitor() {
		this.valTypes = new HashMap<>();
		this.phiValTypes = new HashMap<>();
		this.multiValTypes = new HashMap<>();
		this.varStates = new HashMap<>();

		this.allVars = new HashSet<>();
		this.reifiedVars = new HashSet<>();

		this.seen = new HashSet<>();
		this.open = new ArrayDeque<>();

		this.returnTypes = new HashSet<>();
	}

	public TypeInfo valTypes() {
		return TypeInfo.of(valTypes, phiValTypes, multiValTypes, allVars, reifiedVars, returnType());
	}

	private static TypeSeq returnTypeToTypeSeq(ReturnType rt) {
		if (rt instanceof ReturnType.ConcreteReturnType) {
			return ((ReturnType.ConcreteReturnType) rt).typeSeq;
		}
		else if (rt instanceof ReturnType.TailCallReturnType) {
			Type targetType = ((ReturnType.TailCallReturnType) rt).target;
			if (targetType instanceof FunctionType) {
				FunctionType ft = (FunctionType) targetType;
				return ft.returnTypes();
			}
			else {
				return TypeSeq.vararg();
			}
		}
		else {
			throw new IllegalArgumentException("Illegal return type: " + rt);
		}
	}

	private TypeSeq returnType() {
		TypeSeq ret = null;

		for (ReturnType rt : returnTypes) {
			TypeSeq ts = returnTypeToTypeSeq(rt);
			ret = ret != null ? ret.join(ts) : ts;
		}

		return ret != null ? ret : TypeSeq.vararg();
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
			this.types = Objects.requireNonNull(types);
		}

		public VarState() {
			this(new HashMap<Var, Type>());
		}

		public VarState copy() {
			return new VarState(new HashMap<>(types));
		}

		public void store(Var v, Type t) {
			Objects.requireNonNull(v);
			allVars.add(v);
			types.put(v, Objects.requireNonNull(t));
		}

		public Type load(Var v) {
			Objects.requireNonNull(v);
			allVars.add(v);
			Type t = types.get(Objects.requireNonNull(v));
			if (t == null) {
				throw new IllegalStateException(v + " used before stored into");
			}
			else {
				return t;
			}
		}

		public boolean joinWith(VarState that) {
			Objects.requireNonNull(that);

			Map<Var, Type> result = new HashMap<>();
			for (Var v : this.types.keySet()) {
				Type t = joinTypes(types.get(v), that.types.get(v));
				result.put(v, Objects.requireNonNull(t));
			}
			for (Var v : that.types.keySet()) {
				Type t = joinTypes(types.get(v), that.types.get(v));
				result.put(v, Objects.requireNonNull(t));
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

		public void clearReifiedVars() {
			for (Var v : this.types.keySet()) {
				if (reifiedVars.contains(v)) {
					store(v, LuaTypes.ANY);
				}
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

	private void useStack() {
		// TODO: clear stack state
	}

	private void impure() {
		// TODO: clear upvalue states

		// clear var state of all escaping local variables
		// TODO: could be restricted for variables that escape but are read-only
		currentVarState.clearReifiedVars();
	}

	private void mayCallMetamethod() {
		useStack();
		impure();
	}

	private void assign(Val v, Type t) {
		Objects.requireNonNull(v);
		Objects.requireNonNull(t);

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
		Objects.requireNonNull(pv);
		Objects.requireNonNull(t);

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

	private void assign(MultiVal mv, TypeSeq ts) {
		Objects.requireNonNull(mv);
		Objects.requireNonNull(ts);

		TypeSeq ots = multiValTypes.put(mv, ts);
		if (ts.equals(ots)) {
			// no change
		}
		else if (ots == null) {
			// initial assign
			changed = true;
		}
		else {
			changed = true;
//			throw new IllegalStateException(mv + " assigned to more than once");
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

	private TypeSeq typeOf(MultiVal mv) {
		TypeSeq tseq = multiValTypes.get(mv);
		if (tseq == null) {
			throw new IllegalStateException(mv + " not assigned to yet");
		}
		else {
			return tseq;
		}
	}

	@Override
	public void visit(IRFunc func) {
		Code code = func.code();

		VarState vs = varState(code.entryLabel());
		for (Var p : func.params()) {
			vs.store(p, LuaTypes.DYNAMIC);
		}

		visit(code);
	}

	@Override
	public void visit(Code code) {
		open.add(code.entryLabel());

		while (!open.isEmpty()) {
			visit(code.block(open.poll()));
		}
	}

	@Override
	public void visit(BasicBlock block) {
		boolean firstTimeVisit = seen.add(block.label());

		currentVarState = varState(block.label()).copy();
		changed = false;

		try {
			super.visit(block);

			for (Label nxt : block.end().nextLabels()) {
				VarState vs = varState(nxt);
				if (vs.joinWith(currentVarState)) {
					changed = true;
				}
			}

			if (firstTimeVisit || changed) {
				for (Label nxt : block.end().nextLabels()) {
					open.add(nxt);
				}
			}
		}
		finally {
			changed = false;
			currentVarState = null;
		}
	}

	@Override
	public void visit(LoadConst.Nil node) {
		assign(node.dest(), LuaTypes.NIL);
	}

	@Override
	public void visit(LoadConst.Bool node) {
		assign(node.dest(), LuaTypes.BOOLEAN.newLiteralType(node.value()));
	}

	@Override
	public void visit(LoadConst.Int node) {
		assign(node.dest(), LuaTypes.NUMBER_INTEGER.newLiteralType(node.value()));
	}

	@Override
	public void visit(LoadConst.Flt node) {
		assign(node.dest(), LuaTypes.NUMBER_FLOAT.newLiteralType(node.value()));
	}

	@Override
	public void visit(LoadConst.Str node) {
		assign(node.dest(), LuaTypes.STRING.newLiteralType(node.value()));
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

		Type emulatedResult = Typer.emulateOp(node.op(), l, r);
		if (emulatedResult != null) {
			result = emulatedResult;
		}
		else {
			StaticMathImplementation math = staticMath(node.op());

			if (math != null) {
				NumericOperationType ot = math.opType(l, r);
				result = ot.toType();

				if (ot == NumericOperationType.Any) {
					mayCallMetamethod();
				}
			}
			else {
				switch (node.op()) {
					case CONCAT:
						if (stringable(l) && stringable(r)) {
							result = LuaTypes.STRING;
						}
						else {
							result = LuaTypes.ANY;
							mayCallMetamethod();
						}
						break;

					case EQ:
					case NEQ:
					case LT:
					case LE:
						result = LuaTypes.BOOLEAN;
						mayCallMetamethod();  // TODO: may be restricted (see §2.4 of LRM)
						break;
					default: throw new UnsupportedOperationException("Illegal binary operation: " + node.op());
				}
			}
		}

		assign(node.dest(), result);
	}

	@Override
	public void visit(UnOp node) {
		Type a = typeOf(node.arg());

		final Type result;

		Type emulatedResult = Typer.emulateOp(node.op(), a);
		if (emulatedResult != null) {
			result = emulatedResult;
		}
		else {
			switch (node.op()) {
				case UNM:  result = MAY_BE_INTEGER.opType(a).toType(); break;
				case BNOT: result = MUST_BE_INTEGER.opType(a).toType(); break;
				case NOT:  result = LuaTypes.BOOLEAN; break;
				case LEN:  result = a.isSubtypeOf(LuaTypes.STRING) ? LuaTypes.NUMBER_INTEGER : LuaTypes.ANY; break;
				default: throw new UnsupportedOperationException("Illegal unary operation: " + node.op());
			}
		}

		assign(node.dest(), result);
	}

	@Override
	public void visit(TabNew node) {
		mayCallMetamethod();
		assign(node.dest(), LuaTypes.TABLE);
	}

	@Override
	public void visit(TabGet node) {
		mayCallMetamethod();
		assign(node.dest(), LuaTypes.ANY);
	}

	@Override
	public void visit(TabSet node) {
		mayCallMetamethod();
	}

	@Override
	public void visit(TabRawAppendMulti node) {
		// no effect on vals
	}

	@Override
	public void visit(VarLoad node) {
		Type t = currentVarState().load(node.var());
		assign(node.dest(), t);
	}

	@Override
	public void visit(VarInit node) {
		currentVarState().store(node.var(), typeOf(node.src()));
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
		TypeSeq varargType = TypeSeq.empty().withVararg();  // TODO
		assign(node.dest(), varargType);
	}

	protected TypeSeq vlistType(VList vlist) {
		Type[] fixed = new Type[vlist.addrs().size()];
		for (int i = 0; i < vlist.addrs().size(); i++) {
			fixed[i] = typeOf(vlist.addrs().get(i));
		}

		return vlist.suffix() != null
				? typeOf(vlist.suffix()).prefixedBy(fixed)
				: TypeSeq.of(fixed);
	}

	@Override
	public void visit(Ret node) {
		returnTypes.add(new ReturnType.ConcreteReturnType(vlistType(node.args())));
	}

	@Override
	public void visit(TCall node) {
		returnTypes.add(new ReturnType.TailCallReturnType(typeOf(node.target()), vlistType(node.args())));
	}

	protected TypeSeq callReturnType(Val target, VList args) {
		TypeSeq argTypes = vlistType(args);
		return TypeSeq.empty().withVararg();  // TODO
	}

	@Override
	public void visit(Call node) {
		TypeSeq returnType = callReturnType(node.fn(), node.args());
		assign(node.dest(), returnType);
		impure();
		useStack();
	}

	@Override
	public void visit(MultiGet node) {
		assign(node.dest(), typeOf(node.src()).get(node.idx()));
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
	public void visit(Closure node) {
		for (AbstractVar av : node.args()) {
			if (av instanceof Var) {
				Var v = (Var) av;
				currentVarState().load(v);  // ignoring the result, just marking its use
				reifiedVars.add(v);
			}
		}

		// TODO: look up the type for this closure
		Type t = LuaTypes.FUNCTION;

		assign(node.dest(), t);
	}

	@Override
	public void visit(ToNumber node) {
		Type t = typeOf(node.src());
		Type result = t.isSubtypeOf(LuaTypes.NUMBER) ? t : LuaTypes.NUMBER;

		assign(node.dest(), result);
	}

}
