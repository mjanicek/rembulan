package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

class FunctionVarInfoBuilder {

	private final FunctionVarInfoBuilder parent;

	private final Stack<BlockScope> blockScopes;
	private final List<Variable> params;
	private final List<Variable> locals;
	private final List<Variable.Ref> upvals;
	private boolean varargsUsed;

	public FunctionVarInfoBuilder(FunctionVarInfoBuilder parent) {
		this.parent = parent;
		this.blockScopes = new Stack<>();
		this.params = new ArrayList<>();
		this.locals = new ArrayList<>();
		this.upvals = new ArrayList<>();
		this.varargsUsed = false;

		if (parent == null) {
			upvals.add(Variable.ENV.ref());
		}
	}

	public FunctionVarInfoBuilder parent() {
		return parent;
	}

	public BlockScope enterBlock() {
		BlockScope b = new BlockScope();
		blockScopes.push(b);
		return b;
	}

	public void leaveBlock() {
		blockScopes.pop();
	}

	public Variable addParam(Name n) {
		Variable v = addLocal(n);
		params.add(v);
		return v;
	}

	public Variable addLocal(Name n) {
		Variable v = blockScopes.peek().addLocal(n);
		locals.add(v);
		return v;
	}

	public void setVararg() {
		varargsUsed = true;
	}

	public boolean isVararg() {
		return varargsUsed;
	}

	private Variable findLocal(Name n) {
		for (BlockScope b : blockScopes) {
			Variable v = b.find(n);
			if (v != null) {
				return v;
			}
		}
		return null;
	}

	public ResolvedVariable resolve(Name n) {
		Variable v = findLocal(n);
		if (v != null) {
			return ResolvedVariable.local(v);
		}
		else {
			// it will be an upvalue, the question is from where?
			final Variable w;

			if (parent != null) {
				// ask the parent
				w = parent.resolve(n).variable();
			}
			else {
				// no parent -> it's a global name
				w = Variable.ENV;
			}

			// make sure we know about this upvalue
			if (!upvals.contains(w.ref())) {
				upvals.add(w.ref());
			}

			return ResolvedVariable.upvalue(w);
		}
	}

	public FunctionVarInfo toVarInfo() {
		return new FunctionVarInfo(
				Collections.unmodifiableList(params),
				Collections.unmodifiableList(locals),
				Collections.unmodifiableList(upvals),
				varargsUsed);
	}

	private static class BlockScope {
		public final Stack<Local> locals;

		public BlockScope() {
			this.locals = new Stack<>();
		}

		public Variable addLocal(Name n) {
			Variable v = new Variable(n);
			locals.push(new Local(n, v));
			return v;
		}

		public Variable find(Name n) {
			for (Local l : locals) {
				if (l.name.equals(n)) {
					return l.var;
				}
			}
			return null;
		}

	}

	private static class Local {

		public final Name name;
		public final Variable var;

		public Local(Name name, Variable var) {
			this.name = Check.notNull(name);
			this.var = Check.notNull(var);
		}

	}

}
