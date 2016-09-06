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

import java.util.Arrays;

/**
 * A generic, immutable implementation of the suspended state of a function call, used primarily
 * by compiled Lua functions.
 *
 * <p>The class provides a uniform structured representation of the saved state consisting
 * of</p>
 * <ul>
 *     <li>a <i>resumption point</i>, an integer value, used to select the appropriate
 *       continuation point;</li>
 *     <li>the <i>register state</i>, a sequence of arbitrary objects, typically used to restore
 *       the local variables of the call.</li>
 * </ul>
 *
 * <p>The exact meaning of the values stored in these fields is left to the discretion
 * of the class instantiating this object.</p>
 */
@SuppressWarnings("unused")
public class DefaultSavedState {

	private final int resumptionPoint;
	private final Object[] registers;

	/**
	 * Constructs a new instance of {@code DefaultSavedState} with the specified
	 * {@code resumptionPoint} and {@code registers}.
	 *
	 * @param resumptionPoint  the resumption point
	 * @param registers  the registers, may be {@code null}
	 */
	@SuppressWarnings("unused")
	public DefaultSavedState(int resumptionPoint, Object[] registers) {
		this.resumptionPoint = resumptionPoint;
		this.registers = registers != null ? Arrays.copyOf(registers, registers.length) : null;
	}

	/**
	 * Returns the resumption point stored in this saved state.
	 *
	 * @return  the resumption point
	 */
	@SuppressWarnings("unused")
	public int resumptionPoint() {
		return resumptionPoint;
	}

	/**
	 * Returns a copy of the register state stored in this saved state, or {@code null}
	 * if the register state array stored in this saved state was {@code null}.
	 *
	 * @return  the registers stored in this saved state, possibly {@code null}
	 */
	@SuppressWarnings("unused")
	public Object[] registers() {
		return registers != null
				? Arrays.copyOf(registers, registers.length)
				: null;
	}

}
