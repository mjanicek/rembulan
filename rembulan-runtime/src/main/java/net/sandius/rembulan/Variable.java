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

package net.sandius.rembulan;

/**
 * A reified variable.
 *
 * <p>A variable is an object storing a single value of arbitrary type. When used as fields
 * in function class instances, they serve a function equivalent to that of <i>upvalues</i>
 * in PUC-Lua.</p>
 */
public class Variable {

	private Object value;

	/**
	 * Creates a new variable instance with the given initial value.
	 *
	 * @param initialValue  the initial value of the variable, may be {@code null}
	 */
	public Variable(Object initialValue) {
		this.value = initialValue;
	}

	/**
	 * Gets the value stored in this variable.
	 *
	 * @return  the value of this variable (possibly {@code null})
	 */
	public Object get() {
		return value;
	}

	/**
	 * Sets the value stored in this variable to {@code value}.
	 *
	 * @param value  the new value of this variable, may be {@code null}
	 */
	public void set(Object value) {
		this.value = value;
	}

}
