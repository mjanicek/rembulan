package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.parser.ast.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NameResolutionTransformer extends Transformer {

	private FunctionVarInfoBuilder fnScope;

	public NameResolutionTransformer(FunctionVarInfoBuilder fnScope) {
		this.fnScope = fnScope;
	}

	public NameResolutionTransformer() {
		this(null);
	}

	protected void enterFunction() {
		fnScope = new FunctionVarInfoBuilder(fnScope);
	}

	protected FunctionVarInfo leaveFunction() {
		FunctionVarInfoBuilder scope = fnScope;
		fnScope = scope.parent();
		return scope.toVarInfo();
	}

	@Override
	public Chunk transform(Chunk chunk) {
		enterFunction();
		Chunk c = chunk.update(transform(chunk.block()));
		FunctionVarInfo varInfo = leaveFunction();
		return c.with(varInfo);
	}

	@Override
	public Block transform(Block block) {
		fnScope.enterBlock();
		Block b = super.transform(block);
		fnScope.leaveBlock();
		return b;
	}

	@Override
	public BodyStatement transform(LocalDeclStatement node) {
		List<Name> ns = transformNameList(node.names());
		List<Variable> vs = new ArrayList<>();
		for (Name n : ns) {
			Variable v = fnScope.addLocal(n);
			vs.add(v);
		}
		return node
				.update(ns, transformExprList(node.initialisers()))
				.with(new VarMapping(Collections.unmodifiableList(vs)));
	}

	@Override
	public BodyStatement transform(NumericForStatement node) {
		Name n = transform(node.name());

		Expr init = node.init().accept(this);
		Expr limit = node.limit().accept(this);
		Expr step = node.step() != null ? node.step().accept(this) : null;

		Variable v = fnScope.addLocal(n);

		return node
				.update(n, init, limit, step, transform(node.block()))
				.with(new VarMapping(v));
	}

	@Override
	public BodyStatement transform(GenericForStatement node) {
		List<Name> ns = transformNameList(node.names());
		List<Expr> es = transformExprList(node.exprs());

		List<Variable> vs = new ArrayList<>();
		for (Name n : ns) {
			Variable v = fnScope.addLocal(n);
			vs.add(v);
		}

		return node
				.update(ns, es, transform(node.block()))
				.with(new VarMapping(Collections.unmodifiableList(vs)));
	}

	@Override
	public FunctionDefExpr transform(FunctionDefExpr e) {
		enterFunction();

		fnScope.enterBlock();
		FunctionDefExpr.Params ps = transform(e.params());
		for (Name n : ps.names()) {
			fnScope.addParam(n);
		}

		e = e.update(ps, transform(e.block()));
		fnScope.leaveBlock();

		FunctionVarInfo varInfo = leaveFunction();
		e = e.with(varInfo);

		if (!ps.isVararg() && varInfo.isVararg()) {
			throw new IllegalStateException("cannot use '...' outside a vararg function");
		}

		return e;
	}

	@Override
	public LValueExpr transform(VarExpr e) {
		if (e.attributes().has(ResolvedVariable.class)) {
			throw new IllegalStateException("variable already resolved: " + e.name() + " -> " + e.attributes().get(ResolvedVariable.class));
		}

		ResolvedVariable rv = fnScope.resolve(e.name());

		if (rv.isGlobal()) {
			Attributes attr = e.sourceInfo() != null ? Attributes.of(e.sourceInfo()) : Attributes.empty();

			return new IndexExpr(attr,
					new VarExpr(attr.with(rv), Name.fromString("_ENV")),
					new LiteralExpr(attr, StringLiteral.fromName(e.name())));
		}
		else {
			return e.with(rv);
		}
	}

	@Override
	public Expr transform(VarargsExpr e) {
		fnScope.setVararg();
		return super.transform(e);
	}

	@Override
	public BodyStatement transform(RepeatUntilStatement node) {
		fnScope.enterBlock();
		Block b = super.transform(node.block());
		Expr c = node.condition().accept(this);
		fnScope.leaveBlock();
		return node.update(c, b);
	}

}
