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

package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.Code;
import net.sandius.rembulan.compiler.ir.UpVar;
import net.sandius.rembulan.compiler.ir.Var;

import java.util.List;
import java.util.Objects;

public class IRFunc {

	private final FunctionId id;
	private final List<Var> params;
	private final boolean vararg;
	private final List<UpVar> upvals;
	private final Code code;

	public IRFunc(FunctionId id, List<Var> params, boolean vararg, List<UpVar> upvals, Code code) {
		this.id = Objects.requireNonNull(id);
		this.params = Objects.requireNonNull(params);
		this.vararg = vararg;
		this.upvals = Objects.requireNonNull(upvals);
		this.code = Objects.requireNonNull(code);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IRFunc that = (IRFunc) o;
		return id.equals(that.id)
				&& params.equals(that.params)
				&& upvals.equals(that.upvals)
				&& code.equals(that.code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, params, code);
	}

	public FunctionId id() {
		return id;
	}

	public List<Var> params() {
		return params;
	}

	public boolean isVararg() {
		return vararg;
	}

	public List<UpVar> upvals() {
		return upvals;
	}

	public Code code() {
		return code;
	}

	public IRFunc update(Code code) {
		if (this.code.equals(code)) {
			return this;
		}
		else {
			return new IRFunc(id, params, vararg, upvals, code);
		}
	}

}
