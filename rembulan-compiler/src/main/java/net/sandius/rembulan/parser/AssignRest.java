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

package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.AssignStatement;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.LValueExpr;
import net.sandius.rembulan.parser.ast.SourceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class AssignRest {

	public final SourceInfo src;

	public final List<LValueExpr> vars;
	public final List<Expr> exprs;

	AssignRest(SourceInfo src, List<LValueExpr> vars, List<Expr> exprs) {
		this.src = Objects.requireNonNull(src);
		this.vars = Objects.requireNonNull(vars);
		this.exprs = Objects.requireNonNull(exprs);
	}

	AssignRest(SourceInfo src, List<Expr> exprs) {
		this(src, Collections.<LValueExpr>emptyList(), exprs);
	}

	AssignStatement prepend(LValueExpr v) {
		Objects.requireNonNull(v);
		List<LValueExpr> vs = new ArrayList<>();
		vs.add(v);
		vs.addAll(vars);
		return Statements.assignStatement(src, Collections.unmodifiableList(vs), exprs);
	}

}
