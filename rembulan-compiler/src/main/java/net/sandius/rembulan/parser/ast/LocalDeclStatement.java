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

import java.util.List;
import java.util.Objects;

public class LocalDeclStatement extends BodyStatement {

	private final List<Name> names;
	private final List<Expr> initialisers;

	public LocalDeclStatement(Attributes attr, List<Name> names, List<Expr> initialisers) {
		super(attr);
		this.names = Objects.requireNonNull(names);
		if (names.isEmpty()) {
			throw new IllegalArgumentException("name list must not be empty");
		}
		this.initialisers = Objects.requireNonNull(initialisers);
	}

	public List<Name> names() {
		return names;
	}

	public List<Expr> initialisers() {
		return initialisers;
	}

	public LocalDeclStatement update(List<Name> names, List<Expr> initialisers) {
		if (this.names.equals(names) && this.initialisers.equals(initialisers)) {
			return this;
		}
		else {
			return new LocalDeclStatement(attributes(), names, initialisers);
		}
	}

	public LocalDeclStatement withAttributes(Attributes attr) {
		if (attributes().equals(attr)) return this;
		else return new LocalDeclStatement(attr, names, initialisers);
	}

	public LocalDeclStatement with(Object o) {
		return this.withAttributes(attributes().with(o));
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}
