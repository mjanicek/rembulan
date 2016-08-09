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

public class FunctionType extends ConcreteType {

	// FIXME: masking the field in the superclass!
	protected final AbstractType supertype;

	protected final TypeSeq typeSeq;
	protected final TypeSeq returnTypes;

	FunctionType(AbstractType supertype, TypeSeq arg, TypeSeq ret) {
		super(supertype, supertype.name);
		this.supertype = Check.notNull(supertype);
		this.typeSeq = Check.notNull(arg);
		this.returnTypes = Check.notNull(ret);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FunctionType that = (FunctionType) o;

		return typeSeq.equals(that.typeSeq) && returnTypes.equals(that.returnTypes);
	}

	@Override
	public int hashCode() {
		int result = typeSeq.hashCode();
		result = 31 * result + returnTypes.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return name + argumentTypes().toString() + "->" + returnTypes().toString();
	}

	@Deprecated
	public String toExplicitString() {
		return "(" + argumentTypes().toString() + ") -> (" + returnTypes().toString() + ")";
	}

	public TypeSeq argumentTypes() {
		return typeSeq;
	}

	public TypeSeq returnTypes() {
		return returnTypes;
	}

	@Override
	public boolean isSubtypeOf(Type that) {
		Check.notNull(that);

		if (this.equals(that)) {
			return true;
		}
		if (that instanceof FunctionType) {
			FunctionType ft = (FunctionType) that;

			return ft.argumentTypes().isSubsumedBy(this.argumentTypes())
					&& this.returnTypes().isSubsumedBy(ft.returnTypes());
		}
		else {
			return this.supertype().isSubtypeOf(that);
		}
	}

	@Override
	public Type join(Type that) {
		Check.notNull(that);

		if (this.isSubtypeOf(that)) {
			return that;
		}
		else if (that instanceof FunctionType) {
			FunctionType ft = (FunctionType) that;

			TypeSeq arg = this.argumentTypes().meet(ft.argumentTypes());
			TypeSeq ret = this.returnTypes().join(ft.returnTypes());

			return arg != null && ret != null ? new FunctionType(supertype, arg, ret) : null;
		}
		else {
			return this.supertype().join(that);
		}
	}

	@Override
	public Type meet(Type that) {
		Check.notNull(that);

		if (this.isSubtypeOf(that)) {
			return this;
		}
		else if (that.isSubtypeOf(this)) {
			return that;
		}
		else if (that instanceof FunctionType) {
			FunctionType ft = (FunctionType) that;

			TypeSeq arg = this.argumentTypes().join(ft.argumentTypes());
			TypeSeq ret = this.returnTypes().meet(ft.returnTypes());

			return arg != null && ret != null ? new FunctionType(supertype, arg, ret) : null;
		}
		else {
			return null;
		}
	}

	@Override
	public Type restrict(Type that) {
		if (that instanceof FunctionType) {
			FunctionType thatFt = (FunctionType) that;
			return new FunctionType(supertype,
					this.argumentTypes().restrict(thatFt.argumentTypes()),
					this.returnTypes().restrict(thatFt.returnTypes()));
		}
		else {
			return that instanceof DynamicType ? that : this;
		}
	}

	@Override
	public boolean isConsistentWith(Type that) {
		if (that instanceof FunctionType) {
			FunctionType thatFunc = (FunctionType) that;

			return this.argumentTypes().isConsistentWith(thatFunc.argumentTypes())
					&& this.returnTypes().isConsistentWith(thatFunc.returnTypes());
		}
		else {
			return super.isConsistentWith(that);
		}
	}

//	@Override
//	public Type unionWith(Type that) {
//		return this.restrict(that).join(that.restrict(this));
//	}

}
