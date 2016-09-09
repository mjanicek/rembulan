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
 * A metatable accessor, an interface for getting and setting object metatables.
 *
 * <p>In Lua, only tables and (full) userdata carry their own metatables; for all other
 * types of values <i>T</i>, all values of type <i>T</i> share a metatable. This interface
 * provides a uniform setter for metatables of all types.</p>
 */
public interface MetatableAccessor extends MetatableProvider {

	/**
	 * Sets the metatable for <b>nil</b> (i.e., the {@code nil} type) to {@code table}.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code nil} type, may be {@code null}
	 * @return  the previous metatable for the {@code nil} type
	 */
	Table setNilMetatable(Table table);

	/**
	 * Sets the metatable for the {@code boolean} type.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code boolean} type, may be {@code null}
	 * @return  the previous metatable for the {@code boolean} type
	 */
	Table setBooleanMetatable(Table table);

	/**
	 * Sets the metatable for the {@code number} type.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code number} type, may be {@code null}
	 * @return  the previous metatable for the {@code number} type
	 */
	Table setNumberMetatable(Table table);

	/**
	 * Sets the metatable for the {@code string} type.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code string} type, may be {@code null}
	 * @return  the previous metatable for the {@code string} type
	 */
	Table setStringMetatable(Table table);

	/**
	 * Sets the metatable for the {@code function} type.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code function} type, may be {@code null}
	 * @return  the previous metatable for the {@code function} type
	 */
	Table setFunctionMetatable(Table table);

	/**
	 * Sets the metatable for the {@code thread} type.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code thread} type, may be {@code null}
	 * @return  the previous metatable for the {@code thread} type
	 */
	Table setThreadMetatable(Table table);

	/**
	 * Sets the metatable for light userdata.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for light userdata, may be {@code null}
	 * @return  the previous metatable for light userdata
	 */
	Table setLightUserdataMetatable(Table table);

	/**
	 * Sets the metatable of the object {@code instance} to {@code table}.
	 * {@code table} may be {@code null}: in that case, clears {@code instance}'s metatable.
	 * Returns the previous metatable.
	 *
	 * <p>Note that {@code instance} may share the metatable with other instances of the same
	 * (Lua) type. This method provides a uniform interface for setting the metatables
	 * of all types.</p>
	 *
	 * @param instance  object to set the metatable of, may be {@code null}
	 * @param table  new metatable of {@code instance}, may be {@code null}
	 * @return  the previous metatable of {@code instance}
	 */
	Table setMetatable(Object instance, Table table);

}
