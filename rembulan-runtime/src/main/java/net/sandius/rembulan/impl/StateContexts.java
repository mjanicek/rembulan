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

import net.sandius.rembulan.MetatableAccessor;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.TableFactory;

/**
 * Common implementations of state contexts.
 */
public final class StateContexts {

	private StateContexts() {
		// not to be instantiated
	}

	/**
	 * Returns a new {@code LuaState} with the specified table factory {@code tableFactory}
	 * and the metatable accessor {@code metatableAccessor}.
	 *
	 * @param tableFactory  table factory to be used by this state, must not be {@code null}
	 * @param metatableAccessor  metatable accessor to be used by this state, must not be
	 *                           {@code null}
	 * @return  a new default instance with the specified table factory and metatable accessor
	 *
	 * @throws NullPointerException  if {@code tableFactory} or {@code metatableAccessor}
	 *                               is {@code null}
	 */
	public static StateContext newInstance(TableFactory tableFactory, MetatableAccessor metatableAccessor) {
		return new DefaultStateContext(tableFactory, metatableAccessor);
	}

	/**
	 * Returns a new {@code LuaState} with the default table factory and the default (empty)
	 * metatable accessor.
	 *
	 * @return  a new default instance
	 */
	public static StateContext newDefaultInstance() {
		return newInstance(DefaultTable.factory(), new DefaultMetatableAccessor());
	}

}
