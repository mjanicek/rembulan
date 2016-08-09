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

import net.sandius.rembulan.parser.ast.Name;

public class RegProvider {

	private int valIdx;
	private int phiValIdx;
	private int multiValIdx;

	private int varIdx;

	public RegProvider() {
		this.valIdx = 0;
		this.phiValIdx = 0;
		this.varIdx = 0;
		this.multiValIdx = 0;
	}

	public Val newVal() {
		return new Val(valIdx++);
	}

	public PhiVal newPhiVal() {
		return new PhiVal(phiValIdx++);
	}

	public MultiVal newMultiVal() {
		return new MultiVal(multiValIdx++);
	}

	public Var newVar() {
		return new Var(varIdx++);
	}

	public UpVar newUpVar(Name name) {
		return new UpVar(name);
	}

}
