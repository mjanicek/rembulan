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
 * A Lua state, the global context holding shared metatables and providing methods
 * for instantiating new tables.
 */
@Deprecated
public abstract class LuaState implements MetatableAccessor, TableFactory {

	// TODO: registry

	/**
	 * Returns the metatable for <b>nil</b> (the {@code nil} type).
	 *
	 * @return  the metatable for the {@code nil} type
	 */
	public abstract Table nilMetatable();

	/**
	 * Returns the metatable for {@code boolean} values.
	 *
	 * @return  the metatable for the {@code boolean} type
	 */
	public abstract Table booleanMetatable();

	/**
	 * Returns the metatable for {@code number} values.
	 *
	 * @return  the metatable for the {@code number} type
	 */
	public abstract Table numberMetatable();

	/**
	 * Returns the metatable for {@code string} values.
	 *
	 * @return  the metatable for the {@code string} type
	 */
	public abstract Table stringMetatable();

	/**
	 * Returns the metatable for {@code function} values.
	 *
	 * @return  the metatable for the {@code function} type
	 */
	public abstract Table functionMetatable();

	/**
	 * Returns the metatable for {@code thread} values.
	 *
	 * @return  the metatable for the {@code thread} type
	 */
	public abstract Table threadMetatable();

	/**
	 * Returns the metatable for light userdata.
	 *
	 * @return  the metatable for light userdata
	 */
	public abstract Table lightUserdataMetatable();

	@Override
	public Table getMetatable(Object o) {
		if (o instanceof LuaObject) {
			return ((LuaObject) o).getMetatable();
		}
		else {
			LuaType type = LuaType.typeOf(o);
			switch (type) {
				case NIL: return nilMetatable();
				case BOOLEAN: return booleanMetatable();
				case NUMBER: return numberMetatable();
				case STRING: return stringMetatable();
				case FUNCTION: return functionMetatable();
				case THREAD: return threadMetatable();
				case USERDATA: return lightUserdataMetatable();
				default: throw new IllegalStateException("Illegal type: " + type);
			}
		}
	}

	/**
	 * Sets the metatable for <b>nil</b> (i.e., the {@code nil} type) to {@code table}.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code nil} type, may be {@code null}
	 * @return  the previous metatable for the {@code nil} type
	 */
	public abstract Table setNilMetatable(Table table);

	/**
	 * Sets the metatable for the {@code boolean} type.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code boolean} type, may be {@code null}
	 * @return  the previous metatable for the {@code boolean} type
	 */
	public abstract Table setBooleanMetatable(Table table);

	/**
	 * Sets the metatable for the {@code number} type.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code number} type, may be {@code null}
	 * @return  the previous metatable for the {@code number} type
	 */
	public abstract Table setNumberMetatable(Table table);

	/**
	 * Sets the metatable for the {@code string} type.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code string} type, may be {@code null}
	 * @return  the previous metatable for the {@code string} type
	 */
	public abstract Table setStringMetatable(Table table);

	/**
	 * Sets the metatable for the {@code function} type.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code function} type, may be {@code null}
	 * @return  the previous metatable for the {@code function} type
	 */
	public abstract Table setFunctionMetatable(Table table);

	/**
	 * Sets the metatable for the {@code thread} type.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for the {@code thread} type, may be {@code null}
	 * @return  the previous metatable for the {@code thread} type
	 */
	public abstract Table setThreadMetatable(Table table);

	/**
	 * Sets the metatable for light userdata.
	 * {@code table} may be {@code null}: in that case, clears the metatable. Returns
	 * the previous metatable.
	 *
	 * @param table  new metatable for light userdata, may be {@code null}
	 * @return  the previous metatable for light userdata
	 */
	public abstract Table setLightUserdataMetatable(Table table);

	/**
	 * Sets the metatable of the object {@code o} to {@code table}.
	 * {@code table} may be {@code null}: in that case, clears {@code o}'s metatable. Returns
	 * the previous metatable.
	 *
	 * <p>Note that {@code o} may share the metatable with other instances of the same
	 * (Lua) type. This method provides a uniform interface for setting the metatables
	 * of all types.</p>
	 *
	 * @param o  object to set the metatable of, may be {@code null}
	 * @param table  new metatable of {@code o}, may be {@code null}
	 * @return  the previous metatable of {@code o}
	 */
	@Override
	public Table setMetatable(Object o, Table table) {
		if (o instanceof LuaObject) {
			return ((LuaObject) o).setMetatable(table);
		}
		else {
			LuaType type = LuaType.typeOf(o);
			switch (type) {
				case NIL: return setNilMetatable(table);
				case BOOLEAN: return setBooleanMetatable(table);
				case NUMBER: return setNumberMetatable(table);
				case STRING: return setStringMetatable(table);
				case FUNCTION: return setFunctionMetatable(table);
				case THREAD: return setThreadMetatable(table);
				case USERDATA: return setLightUserdataMetatable(table);
				default: throw new IllegalStateException("Illegal type: " + type);
			}
		}
	}

	/**
	 * Returns the table factory used by this {@code LuaState}.
	 *
	 * @return  the table factory used by this {@code LuaState}
	 */
	public abstract TableFactory tableFactory();

	@Override
	public Table newTable() {
		return tableFactory().newTable();
	}

	@Override
	public Table newTable(int array, int hash) {
		return tableFactory().newTable(array, hash);
	}

}
