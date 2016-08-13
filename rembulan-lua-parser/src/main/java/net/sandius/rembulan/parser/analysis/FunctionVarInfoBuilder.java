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

package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.util.Check;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

class FunctionVarInfoBuilder {

	private final FunctionVarInfoBuilder parent;

	// Used as a stack: not using Stack so as to iterate from top to bottom (see JDK bug [JDK-4475301])
	private final Deque<BlockScope> blockScopes;
	private final List<Variable> params;
	private final List<Variable> locals;
	private final List<Variable.Ref> upvals;
	private boolean varargsUsed;

	public FunctionVarInfoBuilder(FunctionVarInfoBuilder parent) {
		this.parent = parent;
		this.blockScopes = new ArrayDeque<>();
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

	// returns null if n is a not bound to a declaration and is not _ENV
	public ResolvedVariable resolve(Name n) {
		Variable v = findLocal(n);
		if (v != null) {
			return ResolvedVariable.local(v);
		}
		else {
			// an upvalue -- but from where?

			ResolvedVariable result = null;

			if (parent != null) {
				ResolvedVariable p = parent.resolve(n);
				if (p != null) {
					result = ResolvedVariable.upvalue(p.variable());
				}
			}

			if (result == null && n.equals(Variable.ENV_NAME)) {
				result = ResolvedVariable.upvalue(Variable.ENV);
			}

			if (result != null) {
				registerUpvalue(result);
			}

			return result;
		}
	}

	private void registerUpvalue(ResolvedVariable rv) {
		Variable.Ref ref = rv.variable().ref();
		if (rv.isUpvalue() && !upvals.contains(ref)) {
			upvals.add(ref);
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
		// NOTE: we're using a Deque rather than Stack in order to get the correct
		// iteration order (top to bottom) (see JDK bug [JDK-4475301])
		public final Deque<Local> locals;

		public BlockScope() {
			this.locals = new ArrayDeque<>();
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
