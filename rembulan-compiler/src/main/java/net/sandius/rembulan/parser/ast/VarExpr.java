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

import java.util.Objects;

public class VarExpr extends LValueExpr {

	private final Name name;

	public VarExpr(Attributes attr, Name name) {
		super(attr);
		this.name = Objects.requireNonNull(name);
	}

	public Name name() {
		return name;
	}

	public VarExpr withAttributes(Attributes attr) {
		if (attributes().equals(attr)) return this;
		else return new VarExpr(attr, name);
	}

	public VarExpr with(Object o) {
		return this.withAttributes(attributes().with(o));
	}

	@Override
	public LValueExpr accept(Transformer tf) {
		return tf.transform(this);
	}

}
