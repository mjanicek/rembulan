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
 * Base class of objects that have a metatable attached to them on a per-instance basis.
 */
public abstract class LuaObject {

	/**
	 * Returns the metatable of this object, or {@code null} if this object does not have
	 * a metatable.
	 *
	 * @return  this object's metatable, or {@code null} if this object does not have
	 *          a metatable
	 */
	public abstract Table getMetatable();

	/**
	 * Sets the metatable of this object to {@code mt}. {@code mt} may be {@code null}:
	 * in that case, removes the metatable from this object.
	 *
	 * <p>Returns the metatable previously associated with this object (i.e., the metatable
	 * before the call of this method; possibly {@code null}).</p>
	 *
	 * @param mt  new metatable to attach to this object, may be {@code null}
	 * @return  previous metatable associated with this object
	 */
	public abstract Table setMetatable(Table mt);

}
