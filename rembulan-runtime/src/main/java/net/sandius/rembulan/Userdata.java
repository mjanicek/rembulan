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
 * Full userdata.
 *
 * <p>Instances of this class may have a <i>user value</i> attached to them,
 * accessible using the methods {@link #getUserValue()} and {@link #setUserValue(Object)}.</p>
 *
 * <p><b>Note on equality:</b> according to §3.4.4 of the Lua Reference Manual,
 * userdata {@code a} and {@code b} are expected to be equal if and only if they are
 * the same object. However, {@link Ordering#isRawEqual(Object, Object)} compares
 * userdata using {@link Object#equals(Object)}. <b>Exercise caution when overriding
 * {@code equals()}.</b></p>
 */
public abstract class Userdata extends LuaObject {

	/**
	 * Returns the user value attached to this full userdata.
	 *
	 * @return  the user value attached to this full userdata
	 */
	public abstract Object getUserValue();

	/**
	 * Sets the user value attached to this full userdata to {@code value}, returning
	 * the old user value.
	 *
	 * @param value  new user value, may be {@code null}
	 * @return  old user value
	 */
	public abstract Object setUserValue(Object value);

}
