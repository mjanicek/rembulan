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
 * An interface for obtaining metatables for arbitrary values.
 *
 * <p>In Lua, only tables and (full) userdata carry their own metatables; for all other
 * types of values <i>T</i>, all values of type <i>T</i> share a metatable. This interface
 * provides uniform access to metatables of all types.</p>
 */
public interface MetatableProvider {

	/**
	 * Returns the metatable for the object {@code instance}, or {@code null} if this
	 * metatable provider does not assign any metatable to {@code instance}.
	 *
	 * @param instance  the object to obtain a metatable for, may be {@code null}
	 * @return  the metatable of {@code instance}, or {@code null} if there is no metatable
	 *          assigned to {@code instance} in this provider
	 */
	Table getMetatable(Object instance);

}
