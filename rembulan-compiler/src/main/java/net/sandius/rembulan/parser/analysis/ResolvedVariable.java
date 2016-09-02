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

package net.sandius.rembulan.parser.analysis;

import net.sandius.rembulan.util.Check;

public class ResolvedVariable {

	private final boolean upvalue;
	private final Variable var;

	private ResolvedVariable(boolean upvalue, Variable var) {
		this.upvalue = upvalue;
		this.var = Check.notNull(var);
	}

	public static ResolvedVariable local(Variable v) {
		return new ResolvedVariable(false, v);
	}

	public static ResolvedVariable upvalue(Variable v) {
		return new ResolvedVariable(true, v);
	}

	public boolean isUpvalue() {
		return upvalue;
	}

	public Variable variable() {
		return var;
	}

}
