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

import net.sandius.rembulan.compiler.FunctionId;

import java.util.List;
import java.util.Objects;

public class Closure extends BodyNode {

	private final Val dest;
	private final FunctionId id;
	private final List<AbstractVar> args;

	public Closure(Val dest, FunctionId id, List<AbstractVar> args) {
		this.dest = Objects.requireNonNull(dest);
		this.id = Objects.requireNonNull(id);
		this.args = Objects.requireNonNull(args);
	}

	public Val dest() {
		return dest;
	}

	public FunctionId id() {
		return id;
	}

	public List<AbstractVar> args() {
		return args;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
