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

package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabNew extends BodyNode {

	private final Val dest;
	private final int array;
	private final int hash;

	public TabNew(Val dest, int array, int hash) {
		this.dest = Check.notNull(dest);
		this.array = array;
		this.hash = hash;
	}

	public Val dest() {
		return dest;
	}

	public int array() {
		return array;
	}

	public int hash() {
		return hash;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
