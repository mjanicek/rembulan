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

package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;
import java.util.Objects;

public class Block {

	private final List<BodyStatement> statements;
	private final ReturnStatement ret;  // may be null

	public Block(List<BodyStatement> statements, ReturnStatement ret) {
		this.statements = Check.notNull(statements);
		this.ret = ret;
	}

	public List<BodyStatement> statements() {
		return statements;
	}

	public ReturnStatement returnStatement() {
		return ret;
	}

	public Block update(List<BodyStatement> statements, ReturnStatement ret) {
		if (this.statements.equals(statements) && Objects.equals(this.ret, ret)) {
			return this;
		}
		else {
			return new Block(statements, ret);
		}
	}

}
