package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.parser.analysis.FunctionVarInfo;
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

public class IRTranslatorTransformer extends Transformer {

	private final BlockBuilder insns;

	private final RegProvider provider;
	private final Stack<Temp> temps;

	private final Stack<Label> breakLabels;

	private boolean assigning;
	private boolean onStack;

	private final Map<Variable, Var> vars;
	private final Map<Variable, UpVar> uvs;

	public IRTranslatorTransformer() {
		this.insns = new BlockBuilder();

		this.provider = new RegProvider();
		this.temps = new Stack<>();
		this.assigning = false;
		this.onStack = false;
		this.breakLabels = new Stack<>();

		this.vars = new HashMap<>();
		this.uvs = new HashMap<>();
	}

	public Iterator<IRNode> nodes() {
		return insns.nodes();
	}

	private Temp popTemp() {
		if (onStack) {
			onStack = false;
			Temp t = provider.newTemp();
			insns.add(new StackGet(t, 0));
			return t;
		}
		else {
			return temps.pop();
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

	private UpVar upVar(Variable v) {
		UpVar w = uvs.get(v);
		if (w != null) {
			return w;
		}
		else {
			w = provider.newUpVar();
			uvs.put(v, w);
			return w;
		}
	}

	@Override
	public LValueExpr transform(VarExpr e) {
		ResolvedVariable rv = TranslationUtils.resolved(e);

		if (rv.isUpvalue()) {
			// upvalue
			if (assigning) {
				Temp src = popTemp();
				insns.add(new UpStore(upVar(rv.variable()), src));
			}
			else {
				Temp dest = provider.newTemp();
				temps.push(dest);
				insns.add(new UpLoad(dest, upVar(rv.variable())));
			}
		}
		else {
			// local variable
			if (assigning) {
				Temp src = popTemp();
				insns.add(new VarStore(var(rv.variable()), src));
			}
			else {
				Temp dest = provider.newTemp();
				temps.push(dest);
				insns.add(new VarLoad(dest, var(rv.variable())));
			}
		}

		return e;
	}

	@Override
	public LValueExpr transform(IndexExpr e) {
		boolean as = assigning;
		assigning = false;

		e.object().accept(this);
		Temp obj = popTemp();
		e.key().accept(this);
		Temp key = popTemp();

		assigning = as;

		if (assigning) {
			Temp value = popTemp();
			insns.add(new TabSet(obj, key, value));
		}
		else {
			Temp dest = provider.newTemp();
			temps.push(dest);
			insns.add(new TabGet(dest, obj, key));
		}

		return e;
	}

	@Override
	public Literal transform(NilLiteral l) {
		Temp dest = provider.newTemp();
		temps.push(dest);
		insns.add(new LoadConst.Nil(dest));
		return l;
	}

	@Override
	public Literal transform(BooleanLiteral l) {
		Temp dest = provider.newTemp();
		temps.push(dest);
		insns.add(new LoadConst.Bool(dest, l.value()));
		return l;
	}

	@Override
	public Literal transform(Numeral.IntegerNumeral l) {
		Temp dest = provider.newTemp();
		temps.push(dest);
		insns.add(new LoadConst.Int(dest, l.value()));
		return l;
	}

	@Override
	public Literal transform(Numeral.FloatNumeral l) {
		Temp dest = provider.newTemp();
		temps.push(dest);
		insns.add(new LoadConst.Flt(dest, l.value()));
		return l;
	}

	@Override
	public Literal transform(StringLiteral l) {
		Temp dest = provider.newTemp();
		temps.push(dest);
		insns.add(new LoadConst.Str(dest, l.value()));
		return l;
	}

	private void and(Expr left, Expr right) {
		Temp dest = provider.newTemp();
		Label l_false = provider.newLabel();
		Label l_done = provider.newLabel();

		left.accept(this);
		Temp l = popTemp();

		insns.add(new CJmp(l, false, l_false));

		right.accept(this);
		Temp r = popTemp();
		insns.add(new Mov(dest, r));
		insns.add(new Jmp(l_done));

		insns.add(l_false);
		insns.add(new Mov(dest, l));

		insns.add(l_done);
		temps.push(dest);
	}

	private void or(Expr left, Expr right) {
		Temp dest = provider.newTemp();
		Label l_true = provider.newLabel();
		Label l_done = provider.newLabel();

		left.accept(this);
		Temp l = popTemp();

		insns.add(new CJmp(l, true, l_true));

		right.accept(this);
		Temp r = popTemp();
		insns.add(new Mov(dest, r));
		insns.add(new Jmp(l_done));

		insns.add(l_true);
		insns.add(new Mov(dest, l));

		insns.add(l_done);
		temps.push(dest);
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
		Temp l = popTemp();
		right.accept(this);
		Temp r = popTemp();

		Temp dest = provider.newTemp();
		temps.push(dest);

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

		Temp arg = popTemp();
		Temp dest = provider.newTemp();
		temps.push(dest);

		insns.add(new UnOp(TranslationUtils.uop(e.op()), dest, arg));
		return e;
	}

	@Override
	public Expr transform(VarargsExpr e) {
		insns.add(new Vararg());
		onStack = true;
		return e;
	}

	@Override
	public Expr transform(ParenExpr e) {
		e.multiExpr().accept(this);

		Temp dest = popTemp();
		temps.push(dest);

		return e;
	}

	private VList vlist(Temp prefix, List<Expr> exprs) {
		List<Temp> as = new ArrayList<>();
		if (prefix != null) {
			as.add(prefix);
		}

		boolean multi = false;
		Iterator<Expr> it = exprs.iterator();
		while (it.hasNext()) {
			Expr e = it.next();
			e.accept(this);

			if (it.hasNext() || !onStack) {
				as.add(popTemp());
			}
			else {
				// multi-value expression in tail position
//				onStack = false;  // TODO: ?
				multi = true;
			}
		}

		return new VList(Collections.unmodifiableList(as), multi);
	}

	private VList vlist(List<Expr> exprs) {
		return vlist(null, exprs);
	}

	private Call call(CallExpr.FunctionCallExpr e) {
		e.fn().accept(this);
		Temp fn = popTemp();
		VList vl = vlist(e.args());
		return new Call(fn, vl);
	}

	private Call call(CallExpr.MethodCallExpr e) {
		e.target().accept(this);
		Temp obj = popTemp();

		transform(StringLiteral.fromName(e.methodName()));
		Temp method = popTemp();

		Temp fn = provider.newTemp();
		insns.add(new TabGet(fn, obj, method));

		VList vl = vlist(obj, e.args());

		return new Call(fn, vl);
	}

	@Override
	public Expr transform(CallExpr.FunctionCallExpr e) {
		insns.add(call(e));
		onStack = true;
		return e;
	}

	@Override
	public Expr transform(CallExpr.MethodCallExpr e) {
		insns.add(call(e));
		onStack = true;
		return e;
	}

	@Override
	public Expr transform(FunctionDefExpr e) {
		Temp dest = provider.newTemp();

		FunctionVarInfo info = TranslationUtils.funcVarInfo(e);

		List<Var> uvs = new ArrayList<>();
		for (Variable.Ref uv : info.upvalues()) {
			// FIXME
			uvs.add(var(uv.var()));
		}

		insns.add(new Closure(dest, Collections.unmodifiableList(uvs)));

		temps.push(dest);
		return e;
	}

	@Override
	public Expr transform(TableConstructorExpr e) {
		int array = 0;
		int hash = 0;

		Temp dest = provider.newTemp();

		for (TableConstructorExpr.FieldInitialiser fi : e.fields()) {
			if (fi.key() == null) {
				array += 1;
			}
			else {
				hash += 1;
			}
		}

		temps.push(dest);
		insns.add(new TabNew(dest, array, hash));

		int i = 1;
		Iterator<TableConstructorExpr.FieldInitialiser> it = e.fields().iterator();
		while (it.hasNext()) {
			TableConstructorExpr.FieldInitialiser fi = it.next();

			if (fi.key() == null) {
				Temp d = provider.newTemp();
				temps.push(d);
				insns.add(new LoadConst.Int(d, (long) i++));
			}
			else {
				fi.key().accept(this);
			}
			Temp k = popTemp();

			fi.value().accept(this);
			if (fi.key() != null || it.hasNext() || !onStack) {
				Temp v = popTemp();
				insns.add(new TabSet(dest, k, v));
			}
			else {
//				// multi-value expression in tail position
				insns.add(new TabStackAppend(dest));
				onStack = false;  // FIXME: check this
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

	@Override
	public Chunk transform(Chunk chunk) {
		mainBlock(chunk.block());
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
		List<Temp> ts = new ArrayList<>();

		for (Expr e : node.exprs()) {
			e.accept(this);
			ts.add(popTemp());
		}

		Iterator<Temp> it = ts.iterator();
		for (LValueExpr lv : node.vars()) {

			final Temp src;
			if (it.hasNext()) {
				src = it.next();
			}
			else {
				src = provider.newTemp();
				insns.add(new LoadConst.Nil(src));
			}

			temps.push(src);
			assigning = true;
			lv.accept(this);
			assigning = false;
		}

		return node;
	}

	@Override
	public BodyStatement transform(LocalDeclStatement node) {
		List<Temp> ts = new ArrayList<>();

		for (Expr e : node.initialisers()) {
			e.accept(this);
			ts.add(popTemp());
		}

		Iterator<Temp> it = ts.iterator();

		for (Variable w : TranslationUtils.varMapping(node).vars()) {
			Var v = var(w);

			final Temp src;
			if (it.hasNext()) {
				src = it.next();
			}
			else {
				src = provider.newTemp();
				insns.add(new LoadConst.Nil(src));
			}

			insns.add(new VarStore(v, src));
		}

		return node;
	}

	private void condBlock(ConditionalBlock cb, Label l_else, Label l_done) {
		Check.notNull(l_done);

		cb.condition().accept(this);
		Temp c = popTemp();

		insns.add(new CJmp(c, false, l_else != null ? l_else : l_done));
		nestedBlock(cb.block());

		if (l_else != null) {
			insns.add(new Jmp(l_done));
		}
	}

	private Label nextLabel(Iterator<Label> ls) {
		return ls.hasNext() ? ls.next() : null;
	}

	@Override
	public BodyStatement transform(IfStatement node) {
		Label l_done = provider.newLabel();

		List<Label> nexts = new ArrayList<>();
		for (ConditionalBlock cb : node.elifs()) {
			nexts.add(provider.newLabel());
		}
		if (node.elseBlock() != null) {
			nexts.add(provider.newLabel());
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

	private Temp toNumber(Temp addr) {
		Temp t = provider.newTemp();
		insns.add(new ToNumber(t, addr));
		return t;
	}

	private Temp loadConst(int i) {
		Temp t = provider.newTemp();
		insns.add(new LoadConst.Int(t, i));
		return t;
	}

	@Override
	public BodyStatement transform(NumericForStatement node) {
		Label l_top = provider.newLabel();
		Label l_done = provider.newLabel();

		node.init().accept(this);
		Temp t_var0 = toNumber(popTemp());

		node.limit().accept(this);
		Temp t_limit = toNumber(popTemp());

		final Temp t_step;
		if (node.step() != null) {
			node.step().accept(this);
			t_step = toNumber(popTemp());
		}
		else {
			t_step = loadConst(1);
		}

		// var = var - step
		Temp t_var1 = provider.newTemp();
		insns.add(new BinOp(BinOp.Op.SUB, t_var1, t_var0, t_step));

		Var v_var = var(new Variable());  // FIXME
		insns.add(new VarStore(v_var, t_var1));

		insns.add(l_top);

		Temp t_var2 = provider.newTemp();
		insns.add(new VarLoad(t_var2, v_var));

		Temp t_var3 = provider.newTemp();
		insns.add(new BinOp(BinOp.Op.ADD, t_var3, t_var2, t_step));

		// check end-condition
		insns.add(new CheckForEnd(t_var3, t_limit, t_step, l_done));

		insns.add(new VarStore(v_var, t_var3));

		VarMapping vm = TranslationUtils.varMapping(node);
		Var v_v = var(vm.get());
		insns.add(new VarStore(v_v, t_var3));

		breakLabels.push(l_done);
		nestedBlock(node.block());
		breakLabels.pop();

		insns.add(new Jmp(l_top));

		insns.add(l_done);

		return node;
	}

	@Override
	public BodyStatement transform(GenericForStatement node) {
		Label l_top = provider.newLabel();
		Label l_done = provider.newLabel();

		VarMapping vm = TranslationUtils.varMapping(node);

		Temp t_f = provider.newTemp();
		Temp t_s = provider.newTemp();
		Temp t_var0 = provider.newTemp();

		Var v_var = provider.newVar();  // FIXME

		// local f, s, var = explist
		{
			int i = 0;
			for (Expr e : node.exprs()) {
				e.accept(this);

				switch (i) {
					case 0: t_f = popTemp(); break;
					case 1: t_s = popTemp(); break;
					case 2: t_var0 = popTemp(); break;
					default: popTemp(); break;  // discard result
				}

				i += 1;
			}

			// pad with nils if necessary; note that the cases fall through!
			switch (i) {
				case 0: insns.add(new LoadConst.Nil(t_f));
				case 1: insns.add(new LoadConst.Nil(t_s));
				case 2: insns.add(new LoadConst.Nil(t_var0));
				default:
			}

			insns.add(new VarStore(v_var, t_var0));
		}

		insns.add(l_top);

		Temp t_var1 = provider.newTemp();
		insns.add(new VarLoad(t_var1, v_var));

		List<Temp> ts = new ArrayList<>();
		ts.add(t_s);
		ts.add(t_var1);
		insns.add(new Call(t_f, new VList(Collections.unmodifiableList(ts), false)));

		for (int i = 0; i < node.names().size(); i++) {
			Var v = var(vm.get(i));
			Temp t = provider.newTemp();
			insns.add(new StackGet(t, i));
			insns.add(new VarStore(v, t));
		}

		Temp t_v1 = provider.newTemp();
		insns.add(new VarLoad(t_v1, var(vm.get(0))));

		insns.add(new JmpIfNil(t_v1, l_done));

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
		Label l_test = provider.newLabel();
		Label l_done = provider.newLabel();

		insns.add(l_test);
		node.condition().accept(this);
		Temp c = popTemp();
		insns.add(new CJmp(c, false, l_done));

		breakLabels.push(l_done);
		nestedBlock(node.block());
		breakLabels.pop();

		insns.add(new Jmp(l_test));

		insns.add(l_done);

		return node;
	}

	@Override
	public BodyStatement transform(RepeatUntilStatement node) {
		throw new UnsupportedOperationException("repeat-until loop");  // TODO
	}

	@Override
	public BodyStatement transform(LabelStatement node) {
		throw new UnsupportedOperationException("label statement");  // TODO
	}

	@Override
	public BodyStatement transform(GotoStatement node) {
		throw new UnsupportedOperationException("goto statement");  // TODO
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
		return node;
	}

}
