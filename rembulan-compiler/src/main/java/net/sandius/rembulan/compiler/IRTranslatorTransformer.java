package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.parser.analysis.FunctionVarInfo;
import net.sandius.rembulan.parser.analysis.ResolvedLabel;
import net.sandius.rembulan.parser.analysis.ResolvedVariable;
import net.sandius.rembulan.parser.analysis.VarMapping;
import net.sandius.rembulan.parser.analysis.Variable;
import net.sandius.rembulan.parser.ast.*;
import net.sandius.rembulan.parser.ast.util.AttributeUtils;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class IRTranslatorTransformer extends Transformer {

	private final ModuleBuilder moduleBuilder;

	private final FunctionId id;

	private final CodeBuilder insns;

	private final RegProvider provider;
	private final Stack<AbstractVal> vals;
	private final Stack<MultiVal> multis;

	private final Stack<Label> breakLabels;
	private final Map<ResolvedLabel, Label> userLabels;

	private boolean assigning;
//	private boolean onStack;

	private final Map<Variable, Var> vars;
	private final Map<Variable.Ref, UpVar> uvs;

	private final List<Var> params;
	private boolean vararg;
	private final List<UpVar> upvals;

	private int nextNestedFnIdx;

	private IRTranslatorTransformer(ModuleBuilder moduleBuilder, FunctionId id) {
		this.moduleBuilder = Check.notNull(moduleBuilder);
		this.id = Check.notNull(id);

		this.provider = new RegProvider();

		this.insns = new CodeBuilder();

		this.vals = new Stack<>();
		this.multis = new Stack<>();

		this.assigning = false;
//		this.onStack = false;
		this.breakLabels = new Stack<>();
		this.userLabels = new HashMap<>();

		this.vars = new HashMap<>();
		this.uvs = new HashMap<>();

		this.params = new ArrayList<>();
		this.vararg = false;
		this.upvals = new ArrayList<>();

		this.nextNestedFnIdx = 0;
	}

	public IRTranslatorTransformer(ModuleBuilder moduleBuilder) {
		this(moduleBuilder, FunctionId.root());
	}

	private IRFunc result() {
		return new IRFunc(id,
				Collections.unmodifiableList(params),
				vararg,
				Collections.unmodifiableList(upvals),
				insns.build());
	}

	private boolean onStack() {
		return !multis.empty();
	}

	private Val popVal() {
		if (!multis.empty()) {
			Val v = provider.newVal();
			MultiVal mv = multis.pop();
			insns.add(new MultiGet(v, mv, 0));
			return v;
		}
		else {
			AbstractVal aval = vals.pop();
			if (aval instanceof PhiVal) {
				Val v = provider.newVal();
				insns.add(new PhiLoad(v, (PhiVal) aval));
				return v;
			}
			else if (aval instanceof Val) {
				return (Val) aval;
			}
			else {
				throw new UnsupportedOperationException("Unknown abstract value: " + aval);
			}
		}
	}
	
	private Var var(Variable v) {
		Var w = vars.get(v);
		if (w != null) {
			return w;
		}
		else {
			w = provider.newVar();
			vars.put(v, w);
			return w;
		}
	}

	private UpVar upVar(Variable.Ref v) {
		UpVar w = uvs.get(v);
		if (w != null) {
			return w;
		}
		else {
			w = provider.newUpVar(v.var().name());
			uvs.put(v, w);
			return w;
		}
	}

	private Label userLabel(ResolvedLabel rl) {
		Label l = userLabels.get(rl);
		if (l != null) {
			return l;
		}
		else {
			l = insns.newLabel();
			userLabels.put(rl, l);
			return l;
		}
	}

	@Override
	public LValueExpr transform(VarExpr e) {
		ResolvedVariable rv = TranslationUtils.resolved(e);

		if (rv.isUpvalue()) {
			Variable.Ref ref = rv.variable().ref();
			// upvalue
			if (assigning) {
				Val src = popVal();
				insns.add(new UpStore(upVar(ref), src));
			}
			else {
				Val dest = provider.newVal();
				vals.push(dest);
				insns.add(new UpLoad(dest, upVar(ref)));
			}
		}
		else {
			Variable var = rv.variable();
			// local variable
			if (assigning) {
				Val src = popVal();
				insns.add(new VarStore(var(var), src));
			}
			else {
				Val dest = provider.newVal();
				vals.push(dest);
				insns.add(new VarLoad(dest, var(var)));
			}
		}

		return e;
	}

	@Override
	public LValueExpr transform(IndexExpr e) {
		boolean as = assigning;
		assigning = false;

		e.object().accept(this);
		Val obj = popVal();
		e.key().accept(this);
		Val key = popVal();

		assigning = as;

		if (assigning) {
			Val value = popVal();
			insns.add(new TabSet(obj, key, value));
		}
		else {
			Val dest = provider.newVal();
			vals.push(dest);
			insns.add(new TabGet(dest, obj, key));
		}

		return e;
	}

	@Override
	public Literal transform(NilLiteral l) {
		Val dest = provider.newVal();
		vals.push(dest);
		insns.add(new LoadConst.Nil(dest));
		return l;
	}

	@Override
	public Literal transform(BooleanLiteral l) {
		Val dest = provider.newVal();
		vals.push(dest);
		insns.add(new LoadConst.Bool(dest, l.value()));
		return l;
	}

	@Override
	public Literal transform(Numeral.IntegerNumeral l) {
		Val dest = provider.newVal();
		vals.push(dest);
		insns.add(new LoadConst.Int(dest, l.value()));
		return l;
	}

	@Override
	public Literal transform(Numeral.FloatNumeral l) {
		Val dest = provider.newVal();
		vals.push(dest);
		insns.add(new LoadConst.Flt(dest, l.value()));
		return l;
	}

	@Override
	public Literal transform(StringLiteral l) {
		Val dest = provider.newVal();
		vals.push(dest);
		insns.add(new LoadConst.Str(dest, l.value()));
		return l;
	}

	private void and(Expr left, Expr right) {
		PhiVal dest = provider.newPhiVal();
		Label l_false = insns.newLabel();
		Label l_done = insns.newLabel();

		left.accept(this);
		Val l = popVal();

		insns.addBranch(new Branch.Condition.Bool(l, false), l_false);

		right.accept(this);
		Val r = popVal();
		insns.add(new PhiStore(dest, r));
		insns.add(new Jmp(l_done));

		insns.add(l_false);
		insns.add(new PhiStore(dest, l));

		insns.add(l_done);
		vals.push(dest);
	}

	private void or(Expr left, Expr right) {
		PhiVal dest = provider.newPhiVal();
		Label l_true = insns.newLabel();
		Label l_done = insns.newLabel();

		left.accept(this);
		Val l = popVal();

		insns.addBranch(new Branch.Condition.Bool(l, true), l_true);

		right.accept(this);
		Val r = popVal();
		insns.add(new PhiStore(dest, r));
		insns.add(new Jmp(l_done));

		insns.add(l_true);
		insns.add(new PhiStore(dest, l));

		insns.add(l_done);
		vals.push(dest);
	}

	private void eagerBinOp(Operator.Binary op, Expr left, Expr right) {
		BinOp.Op bop = TranslationUtils.bop(op);
		boolean swap = false;

		if (bop == null) {
			bop = TranslationUtils.bop(op.swap());
			swap = true;
		}

		if (bop == null) {
			throw new UnsupportedOperationException("Binary operator not supported: " + op);
		}

		left.accept(this);
		Val l = popVal();
		right.accept(this);
		Val r = popVal();

		Val dest = provider.newVal();
		vals.push(dest);

		insns.add(new BinOp(bop, dest, swap ? r : l, swap ? l : r));
	}

	@Override
	public Expr transform(BinaryOperationExpr e) {
		switch (e.op()) {
			case AND: and(e.left(), e.right()); break;
			case OR: or(e.left(), e.right()); break;
			default: eagerBinOp(e.op(), e.left(), e.right()); break;
		}
		return e;
	}

	@Override
	public Expr transform(UnaryOperationExpr e) {
		e.arg().accept(this);

		Val arg = popVal();
		Val dest = provider.newVal();
		vals.push(dest);

		insns.add(new UnOp(TranslationUtils.uop(e.op()), dest, arg));
		return e;
	}

	@Override
	public Expr transform(VarargsExpr e) {
		MultiVal mv = provider.newMultiVal();
		insns.add(new Vararg(mv));
		multis.push(mv);
		return e;
	}

	@Override
	public Expr transform(ParenExpr e) {
		e.multiExpr().accept(this);

		Val dest = popVal();
		vals.push(dest);

		return e;
	}

	private VList vlist(Val prefix, List<Expr> exprs) {
		List<Val> as = new ArrayList<>();
		if (prefix != null) {
			as.add(prefix);
		}

		MultiVal multi = null;
		Iterator<Expr> it = exprs.iterator();
		while (it.hasNext()) {
			Expr e = it.next();
			e.accept(this);

			if (e instanceof MultiExpr && !it.hasNext() && onStack()) {
				// multi-value expression in tail position
				MultiVal mv = multis.pop();
				multis.clear();  // FIXME
				multi = mv;
			}
			else {
				// single value
				as.add(popVal());
			}
		}

		return new VList(Collections.unmodifiableList(as), multi);
	}

	private VList vlist(List<Expr> exprs) {
		return vlist(null, exprs);
	}

	private Call call(CallExpr.FunctionCallExpr e) {
		e.fn().accept(this);
		Val fn = popVal();
		VList vl = vlist(e.args());

		MultiVal result = provider.newMultiVal();
		return new Call(result, fn, vl);
	}

	private Call call(CallExpr.MethodCallExpr e) {
		e.target().accept(this);
		Val obj = popVal();

		transform(StringLiteral.fromName(e.methodName()));
		Val method = popVal();

		Val fn = provider.newVal();
		insns.add(new TabGet(fn, obj, method));

		VList vl = vlist(obj, e.args());

		MultiVal result = provider.newMultiVal();
		return new Call(result, fn, vl);
	}

	@Override
	public Expr transform(CallExpr.FunctionCallExpr e) {
		Call c = call(e);
		insns.add(c);
		multis.push(c.dest());
		return e;
	}

	@Override
	public Expr transform(CallExpr.MethodCallExpr e) {
		Call c = call(e);
		insns.add(c);
		multis.push(c.dest());
		return e;
	}

	private FunctionId nestedFunc(FunctionVarInfo fi, int idx, Block b) {
		IRTranslatorTransformer visitor = new IRTranslatorTransformer(moduleBuilder, id.child(idx));
		visitor.addParamsAndUpvals(fi);
		visitor.mainBlock(b);
		IRFunc result = visitor.result();
		moduleBuilder.add(result);
		return result.id();
	}

	@Override
	public Expr transform(FunctionDefExpr e) {
		FunctionVarInfo info = TranslationUtils.funcVarInfo(e);

		FunctionId id = nestedFunc(info, nextNestedFnIdx++, e.block());

		Val dest = provider.newVal();

		List<AbstractVar> args = new ArrayList<>();
		for (Variable.Ref uv : info.upvalues()) {
//			// FIXME
//			uvs.add(var(uv.var()));

			if (uvs.containsKey(uv)) {
				// it's an upvalue
				args.add(uvs.get(uv));
			}
			else if (vars.containsKey(uv.var())) {
				// it's a variable
				args.add(vars.get(uv.var()));
			}
			else {
				throw new IllegalStateException("Illegal upvalue: " + uv);
			}
		}

		insns.add(new Closure(dest, id, Collections.unmodifiableList(args)));

		vals.push(dest);
		return e;
	}

	@Override
	public Expr transform(TableConstructorExpr e) {
		int array = 0;
		int hash = 0;

		Val dest = provider.newVal();

		for (TableConstructorExpr.FieldInitialiser fi : e.fields()) {
			if (fi.key() == null) {
				array += 1;
			}
			else {
				hash += 1;
			}
		}

		vals.push(dest);
		insns.add(new TabNew(dest, array, hash));

		// According to the Lua 5.3 Reference Manual, the order of assignment is
		// undefined. PUC-Lua appears to first enter all hash entries (with explicit keys)
		// before array fields (with implicit integer fields).

		// We do the same here.

		{
			// hash part
			Iterator<TableConstructorExpr.FieldInitialiser> it = e.fields().iterator();
			while (it.hasNext()) {
				TableConstructorExpr.FieldInitialiser fi = it.next();

				if (fi.key() != null) {
					fi.key().accept(this);
					Val k = popVal();

					fi.value().accept(this);
					Val v = popVal();

					insns.add(new TabRawSet(dest, k, v));
				}
			}
		}

		{
			// array part
			int i = 1;
			Iterator<TableConstructorExpr.FieldInitialiser> it = e.fields().iterator();
			while (it.hasNext()) {
				TableConstructorExpr.FieldInitialiser fi = it.next();

				if (fi.key() == null) {
					fi.value().accept(this);

					if (!it.hasNext() && onStack()) {
						// appending stack contents
						MultiVal mv = multis.pop();
						insns.add(new TabRawAppendMulti(dest, i, mv));
//						onStack = false;  // FIXME: check this
					}
					else {
						// single value
						Val v = popVal();
						insns.add(new TabRawSetInt(dest, i++, v));
					}
				}
			}
		}

		return e;
	}

	private void nestedBlock(Block b) {
		for (BodyStatement bs : b.statements()) {
			bs.accept(this);
		}
		if (b.returnStatement() != null) {
			b.returnStatement().accept(this);
		}
	}

	private void mainBlock(Block b) {
		nestedBlock(b);
		if (b.returnStatement() == null) {
			insns.add(new Ret(vlist(Collections.<Expr>emptyList())));
		}
	}

	private void addParamsAndUpvals(FunctionVarInfo fi) {
		for (Variable v : fi.params()) {
			Var w = var(v);
			params.add(w);
		}

		vararg = fi.isVararg();

		for (Variable.Ref uv : fi.upvalues()) {
			UpVar u = upVar(uv);  // FIXME: is this correct?
			upvals.add(u);
		}
	}

	@Override
	public Chunk transform(Chunk chunk) {
		addParamsAndUpvals(TranslationUtils.funcVarInfo(chunk));
		mainBlock(chunk.block());
		moduleBuilder.add(result());
		return chunk;
	}

	@Override
	public ReturnStatement transform(ReturnStatement node) {
		if (node.exprs().size() == 1 && node.exprs().get(0) instanceof CallExpr) {
			// tail call

			CallExpr ce = (CallExpr) node.exprs().get(0);

			final Call c;
			if (ce instanceof CallExpr.FunctionCallExpr) {
				c = call((CallExpr.FunctionCallExpr) ce);
			}
			else if (ce instanceof CallExpr.MethodCallExpr) {
				c = call((CallExpr.MethodCallExpr) ce);
			}
			else {
				throw new IllegalStateException("Illegal call expression: " + ce);
			}

			insns.add(new TCall(c.fn(), c.args()));
		}
		else {
			VList args = vlist(node.exprs());
			insns.add(new Ret(args));
		}

		return node;
	}

	@Override
	public BodyStatement transform(AssignStatement node) {
		List<Val> ts = new ArrayList<>();

		for (Expr e : node.exprs()) {
			e.accept(this);
			ts.add(popVal());
		}

		Iterator<Val> it = ts.iterator();
		for (LValueExpr lv : node.vars()) {

			final Val src;
			if (it.hasNext()) {
				src = it.next();
			}
			else {
				src = provider.newVal();
				insns.add(new LoadConst.Nil(src));
			}

			vals.push(src);
			assigning = true;
			lv.accept(this);
			assigning = false;
		}

		return node;
	}

	@Override
	public BodyStatement transform(LocalDeclStatement node) {
		List<Val> ts = new ArrayList<>();

		Iterator<Expr> eit = node.initialisers().iterator();
		while (eit.hasNext()) {
			Expr e = eit.next();
			e.accept(this);

			if (e instanceof MultiExpr && !eit.hasNext() && onStack()) {
				// multiple values at tail position
				// -> ignore for now, will be handled below
			}
			else {
				ts.add(popVal());
			}
		}

		Iterator<Val> it = ts.iterator();

		int i = 0;
		for (Variable w : TranslationUtils.varMapping(node).vars()) {
			Var v = var(w);

			final Val src;
			if (it.hasNext()) {
				src = it.next();
			}
			else {
				src = provider.newVal();
				if (onStack()) {
					insns.add(new MultiGet(src, multis.peek(), i++));
				}
				else {
					insns.add(new LoadConst.Nil(src));
				}
			}

			insns.add(new VarInit(v, src));
		}

		multis.clear();  // FIXME
//		onStack = false;

		return node;
	}

	private void condBlock(ConditionalBlock cb, Label l_else, Label l_done) {
		Check.notNull(l_done);

		cb.condition().accept(this);
		Val c = popVal();

		insns.addBranch(new Branch.Condition.Bool(c, false), l_else != null ? l_else : l_done);
		nestedBlock(cb.block());

		if (l_else != null && insns.isInBlock()) {
			insns.add(new Jmp(l_done));
		}
	}

	private Label nextLabel(Iterator<Label> ls) {
		return ls.hasNext() ? ls.next() : null;
	}

	@Override
	public BodyStatement transform(IfStatement node) {
		Label l_done = insns.newLabel();

		List<Label> nexts = new ArrayList<>();
		for (ConditionalBlock cb : node.elifs()) {
			nexts.add(insns.newLabel());
		}
		if (node.elseBlock() != null) {
			nexts.add(insns.newLabel());
		}

		Iterator<Label> ls = nexts.iterator();

		Label l_next = nextLabel(ls);
		condBlock(node.main(), l_next, l_done);
		for (ConditionalBlock cb : node.elifs()) {
			assert (l_next != null);
			insns.add(l_next);
			l_next = nextLabel(ls);

			condBlock(cb, l_next, l_done);
		}

		if (node.elseBlock() != null) {
			assert (l_next != null);
			insns.add(l_next);
			nestedBlock(node.elseBlock());
		}

		insns.add(l_done);

		return node;
	}

	private Val toNumber(Val addr, String desc) {
		Val t = provider.newVal();
		insns.add(new ToNumber(t, addr, desc));
		return t;
	}

	private Val loadConst(int i) {
		Val t = provider.newVal();
		insns.add(new LoadConst.Int(t, i));
		return t;
	}

	@Override
	public BodyStatement transform(NumericForStatement node) {
		Label l_top = insns.newLabel();
		Label l_done = insns.newLabel();

		// Note: we coerce parameters to numbers in the same order as in PUC Lua to get
		// the same error reporting.

		node.limit().accept(this);
		Val t_limit = toNumber(popVal(), "'for' limit");

		final Val t_step;
		if (node.step() != null) {
			node.step().accept(this);
			t_step = toNumber(popVal(), "'for' step");
		}
		else {
			t_step = loadConst(1);
		}

		node.init().accept(this);
		Val t_var0 = toNumber(popVal(), "'for' initial value");

		// var = var - step
		Val t_var1 = provider.newVal();
		insns.add(new BinOp(BinOp.Op.SUB, t_var1, t_var0, t_step));

		Var v_var = var(new Variable(node.name()));  // FIXME
		insns.add(new VarInit(v_var, t_var1));

		insns.add(l_top);

		Val t_var2 = provider.newVal();
		insns.add(new VarLoad(t_var2, v_var));

		Val t_var3 = provider.newVal();
		insns.add(new BinOp(BinOp.Op.ADD, t_var3, t_var2, t_step));

		// check end-condition
		insns.addBranch(new Branch.Condition.NumLoopEnd(t_var3, t_limit, t_step), l_done);

		insns.add(new VarStore(v_var, t_var3));

		VarMapping vm = TranslationUtils.varMapping(node);
		Var v_v = var(vm.get());
		insns.add(new VarInit(v_v, t_var3));

		breakLabels.push(l_done);
		nestedBlock(node.block());
		breakLabels.pop();

		insns.add(new Jmp(l_top));

		insns.add(l_done);

		return node;
	}

	@Override
	public BodyStatement transform(GenericForStatement node) {
		Label l_top = insns.newLabel();
		Label l_done = insns.newLabel();

		VarMapping vm = TranslationUtils.varMapping(node);

		Val t_f = provider.newVal();
		Val t_s = provider.newVal();
		Val t_var0 = provider.newVal();

		Var v_var = provider.newVar();  // FIXME

		// local f, s, var = explist
		{
			int i = 0;
			Iterator<Expr> eit = node.exprs().iterator();
			while (eit.hasNext()) {
				Expr e = eit.next();
				e.accept(this);

				if (e instanceof MultiExpr && !eit.hasNext() && onStack()) {
					// multiple values at tail position
					// -> ignore, will be dealt with below
				}
				else {
					// single value
					switch (i) {
						case 0: t_f = popVal(); break;
						case 1: t_s = popVal(); break;
						case 2: t_var0 = popVal(); break;
						default: popVal(); break;  // discard result
					}
					i += 1;
				}

			}

			if (onStack()) {
				// fill in the rest from the stack; note that the cases fall through
				MultiVal mv = multis.pop();
				switch (i) {
					case 0: insns.add(new MultiGet(t_f, mv, 0));
					case 1: insns.add(new MultiGet(t_s, mv, 1));
					case 2: insns.add(new MultiGet(t_var0, mv, 2));
					default:
				}
			}
			else {
				// pad with nils if necessary; note that the cases fall through
				switch (i) {
					case 0: insns.add(new LoadConst.Nil(t_f));
					case 1: insns.add(new LoadConst.Nil(t_s));
					case 2: insns.add(new LoadConst.Nil(t_var0));
					default:
				}
			}

			multis.clear();  // FIXME
//			onStack = false;

			insns.add(new VarInit(v_var, t_var0));
		}

		insns.add(l_top);

		Val t_var1 = provider.newVal();
		insns.add(new VarLoad(t_var1, v_var));

		List<Val> ts = new ArrayList<>();
		ts.add(t_s);
		ts.add(t_var1);
		MultiVal mv = provider.newMultiVal();
		insns.add(new Call(mv, t_f, new VList(Collections.unmodifiableList(ts), null)));

		for (int i = 0; i < node.names().size(); i++) {
			Var v = var(vm.get(i));
			Val t = provider.newVal();
			insns.add(new MultiGet(t, mv, i));
			insns.add(new VarInit(v, t));
		}

		Val t_v1 = provider.newVal();
		insns.add(new VarLoad(t_v1, var(vm.get(0))));

		insns.addBranch(new Branch.Condition.Nil(t_v1), l_done);

		insns.add(new VarStore(v_var, t_v1));

		breakLabels.push(l_done);
		nestedBlock(node.block());
		breakLabels.pop();

		insns.add(new Jmp(l_top));

		insns.add(l_done);

		return node;
	}

	@Override
	public BodyStatement transform(WhileStatement node) {
		Label l_test = insns.newLabel();
		Label l_done = insns.newLabel();

		insns.add(l_test);
		node.condition().accept(this);
		Val c = popVal();
		insns.addBranch(new Branch.Condition.Bool(c, false), l_done);

		breakLabels.push(l_done);
		nestedBlock(node.block());
		breakLabels.pop();

		insns.add(new Jmp(l_test));

		insns.add(l_done);

		return node;
	}

	@Override
	public BodyStatement transform(RepeatUntilStatement node) {
		Label l_body = insns.newLabel();
		Label l_done = insns.newLabel();

		insns.add(l_body);
		breakLabels.add(l_done);
		nestedBlock(node.block());
		breakLabels.pop();

		node.condition().accept(this);
		Val c = popVal();
		insns.addBranch(new Branch.Condition.Bool(c, true), l_done);

		insns.add(new Jmp(l_body));

		insns.add(l_done);

		return node;
	}

	@Override
	public BodyStatement transform(LabelStatement node) {
		ResolvedLabel rl = TranslationUtils.resolvedLabel(node);

		insns.add(userLabel(rl));

		return node;
	}

	@Override
	public BodyStatement transform(GotoStatement node) {
		ResolvedLabel rl = TranslationUtils.resolvedLabel(node);

		insns.add(new Jmp(userLabel(rl)));

		return node;
	}

	@Override
	public BodyStatement transform(BreakStatement node) {
		if (breakLabels.isEmpty()) {
			throw new IllegalStateException("<break> at " + AttributeUtils.sourceInfoString(node) + " not inside a loop");
		}
		else {
			Label l = breakLabels.peek();
			insns.add(new Jmp(l));
		}
		return node;
	}

	@Override
	public BodyStatement transform(CallStatement node) {
		node.callExpr().accept(this);

		// discard results
		multis.clear();  // FIXME
//		onStack = false;

		return node;
	}

}
