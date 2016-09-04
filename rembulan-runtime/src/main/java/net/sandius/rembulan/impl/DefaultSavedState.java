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

package net.sandius.rembulan.impl;

import net.sandius.rembulan.util.Check;

import java.util.Arrays;

public class DefaultSavedState {

	private final int resumptionPoint;
	private final Object[] registers;
	private final Object[] varargs;  // may be null

	public DefaultSavedState(int resumptionPoint, Object[] registers, Object[] varargs) {
		Check.notNull(registers);
		this.resumptionPoint = resumptionPoint;
		this.registers = Arrays.copyOf(registers, registers.length);
		this.varargs = varargs != null ? Arrays.copyOf(varargs, varargs.length) : null;
	}

	public DefaultSavedState(int resumptionPoint, Object[] registers) {
		this(resumptionPoint, registers, null);
	}

	public int resumptionPoint() {
		return resumptionPoint;
	}

	public Object[] registers() {
		return registers;
	}

	public Object[] varargs() {
		return varargs != null ? varargs : null;
	}

}
