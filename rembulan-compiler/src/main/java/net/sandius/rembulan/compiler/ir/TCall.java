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

import java.util.Collections;
import java.util.Objects;

public class TCall extends BlockTermNode {

	private final Val target;
	private final VList args;

	public TCall(Val target, VList args) {
		this.target = Objects.requireNonNull(target);
		this.args = Objects.requireNonNull(args);
	}

	public Val target() {
		return target;
	}

	public VList args() {
		return args;
	}

	@Override
	public Iterable<Label> nextLabels() {
		return Collections.emptyList();
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}
