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

import java.util.Objects;

public class CPUWithdraw extends BodyNode {

	private final int cost;

	public CPUWithdraw(int cost) {
		this.cost = Check.positive(cost);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CPUWithdraw that = (CPUWithdraw) o;
		return this.cost == that.cost;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cost);
	}

	public int cost() {
		return cost;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
