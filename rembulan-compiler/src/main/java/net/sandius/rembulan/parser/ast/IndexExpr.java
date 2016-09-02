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

public class IndexExpr extends LValueExpr {

	private final Expr object;
	private final Expr key;

	public IndexExpr(Attributes attr, Expr object, Expr key) {
		super(attr);
		this.object = Check.notNull(object);
		this.key = Check.notNull(key);
	}

	public Expr object() {
		return object;
	}

	public Expr key() {
		return key;
	}

	public IndexExpr update(Expr object, Expr key) {
		if (this.object.equals(object) && this.key.equals(key)) {
			return this;
		}
		else {
			return new IndexExpr(attributes(), object, key);
		}
	}

	@Override
	public LValueExpr accept(Transformer tf) {
		return tf.transform(this);
	}

}
