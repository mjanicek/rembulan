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

import java.util.List;
import java.util.Objects;

public class BasicBlock {

	private final Label label;
	private final List<BodyNode> body;
	private final BlockTermNode end;

	public BasicBlock(Label label, List<BodyNode> body, BlockTermNode end) {
		this.label = Check.notNull(label);
		this.body = Check.notNull(body);
		this.end = Check.notNull(end);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BasicBlock that = (BasicBlock) o;
		return this.label.equals(that.label) &&
				this.body.equals(that.body) &&
				this.end.equals(that.end);
	}

	@Override
	public int hashCode() {
		return Objects.hash(label, body, end);
	}

	public Label label() {
		return label;
	}

	public List<BodyNode> body() {
		return body;
	}

	public BlockTermNode end() {
		return end;
	}

}
