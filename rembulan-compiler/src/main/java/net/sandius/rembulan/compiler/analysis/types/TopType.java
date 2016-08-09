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

public final class TopType extends AbstractType {

	public TopType(String name) {
		super(null, name);
	}

	@Override
	public boolean isSubtypeOf(Type that) {
		return this.equals(that);
	}

	@Override
	public Type restrict(Type that) {
		return that instanceof DynamicType ? that : this;
	}

	@Override
	public Type join(Type that) {
		return this;
	}

	@Override
	public Type meet(Type that) {
		return that;
	}

//	@Override
//	public Type unionWith(Type that) {
//		return this;
//	}

}
