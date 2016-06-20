package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.parser.analysis.ResolvedVariable;
import net.sandius.rembulan.parser.analysis.Variable;
import net.sandius.rembulan.parser.ast.*;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class IRTranslatorTransformer extends Transformer {

	private final RegProvider provider;
	private final Stack<Temp> temps;
	private final List<IRNode> insns;
	private boolean assigning;
	private boolean onStack;

	private final Map<Variable, Var> vars;
	private final Map<Variable, UpVar> uvs;

	public IRTranslatorTransformer() {
		this.provider = new RegProvider();
		this.temps = new Stack<>();
		this.insns = new ArrayList<>();
		this.assigning = false;
		this.onStack = false;

		this.vars = new HashMap<>();
		this.uvs = new HashMap<>();
	}

	public List<IRNode> nodes() {
		return insns;
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
				insns.add(new UpStore(upVar(rv.variable())));
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
				insns.add(new VarStore(var(rv.variable())));
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
		e.object().accept(this);
		Temp obj = popTemp();
		e.key().accept(this);
		Temp key = popTemp();

		Temp dest = provider.newTemp();
		temps.push(dest);

		Check.isFalse(assigning);  // FIXME

		insns.add(new TabGet(dest, obj, key));

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

	@Override
	public Expr transform(BinaryOperationExpr e) {
		BinOp.Op op = TranslationUtils.bop(e.op());
		boolean swap = false;

		if (op == null) {
			op = TranslationUtils.bop(e.op().swap());
			swap = true;
		}

		if (op == null) {
			throw new UnsupportedOperationException("Binary operator not supported: " + e.op());
		}

		Expr l = swap ? e.right() : e.left();
		Expr r = swap ? e.left() : e.right();

		l.accept(this);
		Temp left = popTemp();
		r.accept(this);
		Temp right = popTemp();

		Temp dest = provider.newTemp();
		temps.push(dest);

		insns.add(new BinOp(op, dest, left, right));
		
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

	@Override
	public Expr transform(CallExpr.FunctionCallExpr e) {
		e.fn().accept(this);
		Temp fn = popTemp();

		List<Temp> as = new ArrayList<>();
		for (Expr a : e.args()) {
			a.accept(this);
			as.add(popTemp());
		}

		insns.add(new Call(fn, new VList.Fixed(Collections.unmodifiableList(as))));

		onStack = true;

		return e;
	}

	@Override
	public Expr transform(CallExpr.MethodCallExpr e) {
		e.target().accept(this);
		Temp fn = popTemp();

		transform(StringLiteral.fromName(e.methodName()));
		Temp n = popTemp();

		Temp callTgt = provider.newTemp();
		insns.add(new TabGet(callTgt, fn, n));

		List<Temp> as = new ArrayList<>();
		as.add(fn);
		for (Expr a : e.args()) {
			a.accept(this);
			as.add(popTemp());
		}

		insns.add(new Call(callTgt, new VList.Fixed(Collections.unmodifiableList(as))));

		onStack = true;

		return e;
	}

	@Override
	public Expr transform(FunctionDefExpr e) {
		throw new UnsupportedOperationException();  // TODO
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
			if (it.hasNext() || !onStack) {
				Temp v = popTemp();
				insns.add(new TabSet(dest, k, v));
			}
			else {
//				// multi-value expression in tail position
				insns.add(new TabStackAppend(dest));
				onStack = false;
//				throw new UnsupportedOperationException();  // TODO
			}
		}

		return e;
	}

	@Override
	public ReturnStatement transform(ReturnStatement node) {
		if (node.exprs().size() == 1 && node.exprs().get(0) instanceof CallExpr) {
			// tail call
			throw new UnsupportedOperationException("tail call");  // TODO
		}
		else {
			List<Temp> args = new ArrayList<>();

			Iterator<Expr> it = node.exprs().iterator();
			while (it.hasNext()) {
				Expr e = it.next();
				e.accept(this);
				if (it.hasNext() || !onStack) {
					args.add(popTemp());
				}
				else {
					// multi-value expression in tail position
					throw new UnsupportedOperationException("multi-value expr in tail position");  // TODO
				}
			}

			insns.add(new Ret(Collections.unmodifiableList(args)));
		}

		return node;
	}

}
