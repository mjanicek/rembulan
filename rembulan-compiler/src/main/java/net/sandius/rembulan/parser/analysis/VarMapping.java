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

import java.util.Collections;
import java.util.List;

public class VarMapping {

	private final List<Variable> vars;

	public VarMapping(List<Variable> vars) {
		Check.notNull(vars);
		if (vars.isEmpty()) {
			throw new IllegalArgumentException("variable list is empty");
		}
		this.vars = vars;
	}

	public VarMapping(Variable v) {
		this(Collections.singletonList(Check.notNull(v)));
	}

	public List<Variable> vars() {
		return vars;
	}

	public Variable get(int idx) {
		return vars.get(idx);
	}

	public Variable get() {
		return get(0);
	}

}
