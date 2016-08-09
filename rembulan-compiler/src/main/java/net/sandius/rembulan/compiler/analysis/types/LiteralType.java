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

package net.sandius.rembulan.compiler.analysis.types;

import net.sandius.rembulan.util.Check;

public class LiteralType<T> extends Type {

	private final ConcreteType type;
	private final T value;

	public LiteralType(ConcreteType type, T value) {
		this.type = Check.notNull(type);
		this.value = Check.notNull(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiteralType<?> that = (LiteralType<?>) o;

		if (!type.equals(that.type)) return false;
		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		int result = type.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return type.toString() + "(" + value + ")";
	}

	public ConcreteType type() {
		return type;
	}

	public T value() {
		return value;
	}

	@Override
	public boolean isSubtypeOf(Type that) {
		return this.equals(that) || this.type().isSubtypeOf(that);
	}

	@Override
	public Type restrict(Type that) {
		return that instanceof DynamicType ? that : this;
	}

	@Override
	public Type join(Type that) {
		Check.notNull(that);

		if (that.isSubtypeOf(this)) return this;
		else return this.type().join(that);
	}

	@Override
	public Type meet(Type that) {
		Check.notNull(that);

		if (this.isSubtypeOf(that)) return this;
		else if (that.isSubtypeOf(this)) return that;
		else return null;
	}

}
