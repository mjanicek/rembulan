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

import java.util.List;
import java.util.Objects;

public class FunctionVarInfo {

	private final List<Variable> params;
	private final List<Variable> locals;
	private final List<Variable.Ref> upvalues;
	private final boolean vararg;

	public FunctionVarInfo(List<Variable> params, List<Variable> locals, List<Variable.Ref> upvalues, boolean vararg) {
		this.params = Objects.requireNonNull(params);
		this.locals = Objects.requireNonNull(locals);
		this.upvalues = Objects.requireNonNull(upvalues);
		this.vararg = vararg;
	}

	public List<Variable> params() {
		return params;
	}

	public List<Variable> locals() {
		return locals;
	}

	public List<Variable.Ref> upvalues() {
		return upvalues;
	}

	public boolean isVararg() {
		return vararg;
	}

}
