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

/**
 * Default implementation of a suspended function call state, used primarily by compiled
 * Lua functions.
 *
 * <p>The class provides a uniform structured representation of the saved state consisting
 * of</p>
 * <ul>
 *     <li>a <i>resumption point</i>, an integer value, typically used to select the
 *       appropriate continuation point</li>
 *     <li><i>register state</i>, an array of arbitrary objects, typically used to restore
 *       the local variables</li>
 *     <li>additional call arguments (<i>varargs</i>) to the call</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class DefaultSavedState {

	private final int resumptionPoint;
	private final Object[] registers;

	// TODO: remove: can store varargs in the registers
	private final Object[] varargs;  // may be null

	/**
	 * Constructs a new instance of {@code DefaultSavedState} with the specified
	 * {@code resumptionPoint}, {@code registers} and {@code varargs}.
	 *
	 * @param resumptionPoint  the resumption point
	 * @param registers  the registers, must not be {@code null}
	 * @param varargs  varargs, may be {@code null}
	 *
	 * @throws IllegalArgumentException  if {@code registers} is {@code null}
	 */
	@Deprecated
	@SuppressWarnings("unused")
	public DefaultSavedState(int resumptionPoint, Object[] registers, Object[] varargs) {
		Check.notNull(registers);
		this.resumptionPoint = resumptionPoint;
		this.registers = Arrays.copyOf(registers, registers.length);
		this.varargs = varargs != null ? Arrays.copyOf(varargs, varargs.length) : null;
	}

	/**
	 * Constructs a new instance of {@code DefaultSavedState} with the specified
	 * {@code resumptionPoint} and {@code registers} and without varargs.
	 *
	 * @param resumptionPoint  the resumption point
	 * @param registers  the registers, must not be {@code null}
	 *
	 * @throws IllegalArgumentException  if {@code registers} is {@code null}
	 */
	@SuppressWarnings("unused")
	public DefaultSavedState(int resumptionPoint, Object[] registers) {
		this(resumptionPoint, registers, null);
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
	 * Returns the register state stored in this saved state.
	 *
	 * @return  the registers stored in this saved state
	 */
	@SuppressWarnings("unused")
	public Object[] registers() {
		// TODO: return a copy
		return registers;
	}

	/**
	 * Returns the varargs stored in this saved state.
	 *
	 * @return  the varargs stored in this saved state
	 */
	@Deprecated
	@SuppressWarnings("unused")
	public Object[] varargs() {
		return varargs;
	}

}
