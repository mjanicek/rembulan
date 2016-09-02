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

package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.io.Serializable;

public class DefaultSavedState implements Serializable {

	private final int resumptionPoint;
	private final ReadOnlyArray<Object> registers;
	private final ReadOnlyArray<Object> varargs;

	public DefaultSavedState(int resumptionPoint, Object[] registers, Object[] varargs) {
		this.resumptionPoint = resumptionPoint;
		this.registers = ReadOnlyArray.copyFrom(Check.notNull(registers));
		this.varargs = varargs != null ? ReadOnlyArray.copyFrom(varargs) : null;
	}

	public DefaultSavedState(int resumptionPoint, Object[] registers) {
		this(resumptionPoint, registers, null);
	}

	public int resumptionPoint() {
		return resumptionPoint;
	}

	public Object[] registers() {
		return registers.copyToNewArray();
	}

	public Object[] varargs() {
		return varargs != null ? varargs.copyToNewArray() : null;
	}

}
