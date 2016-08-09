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

public class ConcreteType extends Type {

	protected final AbstractType supertype;
	protected final String name;

	protected ConcreteType(AbstractType supertype, String name) {
		this.supertype = Check.notNull(supertype);
		this.name = Check.notNull(name);
	}

	@Override
	public String toString() {
		return name;
	}

	public AbstractType supertype() {
		return supertype;
	}

	@Override
	public boolean isSubtypeOf(Type that) {
		return this.equals(that) || this.supertype().isSubtypeOf(that);
	}

	@Override
	public Type restrict(Type that) {
		return that instanceof DynamicType ? that : this;
	}

	@Override
	public Type join(Type that) {
		Check.notNull(that);

		if (that.isSubtypeOf(this)) return this;
		else return this.supertype().join(that);
	}

	@Override
	public Type meet(Type that) {
		Check.notNull(that);

		if (this.isSubtypeOf(that)) return this;
		else if (that.isSubtypeOf(this)) return that;
		else return null;
	}

	//	@Override
//	public Type unionWith(Type that) {
//		if (this.isSubtypeOf(that)) return that;
//		else if (that.isSubtypeOf(this)) return this;
//		else {
//			Type t = this.join(that);
//			if (t != null) return t;
//			else return DynamicType.INSTANCE;  // FIXME: is this correct?
//		}
//	}

}
