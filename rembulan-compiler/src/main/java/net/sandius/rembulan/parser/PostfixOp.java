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

import net.sandius.rembulan.parser.ast.CallExpr;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.IndexExpr;
import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.parser.ast.SourceInfo;

import java.util.List;
import java.util.Objects;

abstract class PostfixOp {

	public abstract Expr on(Expr exp);

	static class FieldAccess extends PostfixOp {

		private final SourceInfo src;
		private final Expr keyExpr;

		public FieldAccess(SourceInfo src, Expr keyExpr) {
			this.src = Objects.requireNonNull(src);
			this.keyExpr = Objects.requireNonNull(keyExpr);
		}

		public Expr keyExpr() {
			return keyExpr;
		}

		@Override
		public IndexExpr on(Expr exp) {
			return Exprs.index(src, exp, keyExpr);
		}

	}

	static class Invoke extends PostfixOp {

		private final Name method;  // may be null
		private final SourceElement<List<Expr>> args;

		public Invoke(SourceElement<List<Expr>> args, Name method) {
			this.args = Objects.requireNonNull(args);
			this.method = method;
		}

		@Override
		public CallExpr on(Expr exp) {
			return method != null
					? Exprs.methodCall(args.sourceInfo(), exp, method, args.element())
					: Exprs.functionCall(args.sourceInfo(), exp, args.element());
		}

	}

}
