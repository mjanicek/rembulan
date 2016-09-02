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

package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.List;

public class FunctionDefExpr extends Expr {

	private final Params params;
	private final Block block;

	public FunctionDefExpr(Attributes attr, Params params, Block block) {
		super(attr);
		this.params = Check.notNull(params);
		this.block = Check.notNull(block);
	}

	public Params params() {
		return params;
	}

	public Block block() {
		return block;
	}

	public FunctionDefExpr update(Params params, Block block) {
		if (this.params.equals(params) && this.block.equals(block)) {
			return this;
		}
		else {
			return new FunctionDefExpr(attributes(), params, block);
		}
	}

	public FunctionDefExpr withAttributes(Attributes attr) {
		if (attributes().equals(attr)) return this;
		else return new FunctionDefExpr(attr, params, block);
	}

	public FunctionDefExpr with(Object o) {
		return this.withAttributes(attributes().with(o));
	}

	@Override
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

	public static class Params {

		private final List<Name> names;
		private final boolean vararg;

		public Params(List<Name> names, boolean vararg) {
			this.names = Check.notNull(names);
			this.vararg = vararg;
		}

		public static Params empty() {
			return new Params(Collections.<Name>emptyList(), false);
		}

		public static Params emptyVararg() {
			return new Params(Collections.<Name>emptyList(), true);
		}

		public List<Name> names() {
			return names;
		}

		public boolean isVararg() {
			return vararg;
		}

		public Params update(List<Name> names, boolean vararg) {
			if (this.names.equals(names) && this.vararg == vararg) {
				return this;
			}
			else {
				return new Params(names, vararg);
			}
		}

	}

}
