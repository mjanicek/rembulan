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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Branch extends BlockTermNode implements JmpNode {

	private final Condition condition;
	private final Label branch;
	private final Label next;

	public Branch(Condition condition, Label branch, Label next) {
		this.condition = Objects.requireNonNull(condition);
		this.branch = Objects.requireNonNull(branch);
		this.next = Objects.requireNonNull(next);
	}

	public Condition condition() {
		return condition;
	}

	@Override
	public Label jmpDest() {
		return branch;
	}

	public Label next() {
		return next;
	}

	@Override
	public Iterable<Label> nextLabels() {
		List<Label> tmp = new ArrayList<>(2);
		tmp.add(next());
		tmp.add(jmpDest());
		return Collections.unmodifiableList(tmp);
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

	public static abstract class Condition {

		private Condition() {
			// not to be instantiated by the outside world
		}

		public abstract void accept(IRVisitor visitor);

		public static class Nil extends Condition {

			private final Val addr;

			public Nil(Val addr) {
				this.addr = Objects.requireNonNull(addr);
			}

			public Val addr() {
				return addr;
			}

			@Override
			public void accept(IRVisitor visitor) {
				visitor.visit(this);
			}

		}

		public static class Bool extends Condition {

			private final Val addr;
			private final boolean expected;

			public Bool(Val addr, boolean expected) {
				this.addr = Objects.requireNonNull(addr);
				this.expected = expected;
			}

			public Val addr() {
				return addr;
			}

			public boolean expected() {
				return expected;
			}

			@Override
			public void accept(IRVisitor visitor) {
				visitor.visit(this);
			}

		}

		public static class NumLoopEnd extends Condition {

			private final Val var;
			private final Val limit;
			private final Val step;

			public NumLoopEnd(Val var, Val limit, Val step) {
				this.var = Objects.requireNonNull(var);
				this.limit = Objects.requireNonNull(limit);
				this.step = Objects.requireNonNull(step);
			}

			public Val var() {
				return var;
			}

			public Val limit() {
				return limit;
			}

			public Val step() {
				return step;
			}

			@Override
			public void accept(IRVisitor visitor) {
				visitor.visit(this);
			}

		}

	}

}
