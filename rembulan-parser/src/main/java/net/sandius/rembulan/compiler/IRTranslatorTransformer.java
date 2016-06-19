package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.parser.analysis.ResolvedVariable;
import net.sandius.rembulan.parser.ast.*;

import java.util.ArrayList;
import java.util.List;

public class IRTranslatorTransformer extends Transformer {

	private final List<IRNode> insns;
	private boolean assigning;

	public IRTranslatorTransformer() {
		this.insns = new ArrayList<>();
		this.assigning = false;
	}

	public List<IRNode> nodes() {
		return insns;
	}

	@Override
	public LValueExpr transform(VarExpr e) {
		ResolvedVariable rv = TranslationUtils.resolved(e);

		if (rv.isUpvalue()) {
			// upvalue
			insns.add(assigning
					? new UpStore(TranslationUtils.upVar(rv.variable()))
					: new UpLoad(TranslationUtils.upVar(rv.variable())));
		}
		else {
			// local variable
			insns.add(assigning
					? new VarStore(TranslationUtils.var(rv.variable()))
					: new VarLoad(TranslationUtils.var(rv.variable())));
		}

		return e;
	}

	@Override
	public LValueExpr transform(IndexExpr e) {
		e.object().accept(this);
		e.key().accept(this);
		insns.add(assigning
				? new TabSet()
				: new TabGet());
		return e;
	}

	@Override
	public Literal transform(NilLiteral l) {
		insns.add(new LoadConst.Nil());
		return l;
	}

	@Override
	public Literal transform(BooleanLiteral l) {
		insns.add(new LoadConst.Bool(l.value()));
		return l;
	}

	@Override
	public Literal transform(Numeral.IntegerNumeral l) {
		insns.add(new LoadConst.Int(l.value()));
		return l;
	}

	@Override
	public Literal transform(Numeral.FloatNumeral l) {
		insns.add(new LoadConst.Flt(l.value()));
		return l;
	}

	@Override
	public Literal transform(StringLiteral l) {
		insns.add(new LoadConst.Str(l.value()));
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
		r.accept(this);

		insns.add(new BinOp(op));
		
		return e;
	}

	@Override
	public Expr transform(UnaryOperationExpr e) {
		e.arg().accept(this);
		insns.add(new UnOp(TranslationUtils.uop(e.op())));
		return e;
	}

	@Override
	public Expr transform(VarargsExpr e) {
		insns.add(new Vararg());

		// FIXME -- decided by the consumer!
		insns.add(new ArrayGet(0));

		return e;
	}

	@Override
	public Expr transform(CallExpr.FunctionCallExpr e) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Expr transform(CallExpr.MethodCallExpr e) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Expr transform(FunctionDefExpr e) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public Expr transform(TableConstructorExpr e) {
		throw new UnsupportedOperationException();  // TODO
	}

}
