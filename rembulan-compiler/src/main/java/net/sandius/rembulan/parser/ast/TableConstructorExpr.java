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

import java.util.List;
import java.util.Objects;

public class TableConstructorExpr extends Expr {

	private final List<FieldInitialiser> fields;

	public TableConstructorExpr(Attributes attr, List<FieldInitialiser> fields) {
		super(attr);
		this.fields = Check.notNull(fields);
	}

	public static class FieldInitialiser {

		private final Expr keyExpr;  // may be null
		private final Expr valueExpr;

		public FieldInitialiser(Expr keyExpr, Expr valueExpr) {
			this.keyExpr = keyExpr;
			this.valueExpr = Check.notNull(valueExpr);
		}

		public Expr key() {
			return keyExpr;
		}

		public Expr value() {
			return valueExpr;
		}

		public FieldInitialiser update(Expr keyExpr, Expr valueExpr) {
			if (Objects.equals(this.keyExpr, keyExpr) && this.valueExpr.equals(valueExpr)) {
				return this;
			}
			else {
				return new FieldInitialiser(keyExpr, valueExpr);
			}
		}

	}

	public List<FieldInitialiser> fields() {
		return fields;
	}

	public TableConstructorExpr update(List<FieldInitialiser> fields) {
		if (this.fields.equals(fields)) {
			return this;
		}
		else {
			return new TableConstructorExpr(attributes(), fields);
		}
	}

	@Override
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

}
