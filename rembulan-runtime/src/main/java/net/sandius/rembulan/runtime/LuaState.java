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

package net.sandius.rembulan.runtime;

import net.sandius.rembulan.MetatableAccessor;
import net.sandius.rembulan.TableFactory;
import net.sandius.rembulan.exec.CallInitialiser;
import net.sandius.rembulan.exec.OneShotContinuation;
import net.sandius.rembulan.impl.DefaultTable;

/**
 * Default implementation of a state context that is also a call initialiser.
 */
public class LuaState extends AbstractStateContext implements CallInitialiser {

	/**
	 * Constructs a new {@code LuaState} with the specified table factory
	 * {@code tableFactory} and the metatable accessor {@code metatableAccessor}.
	 *
	 * @param tableFactory  table factory to be used by this state, must not be {@code null}
	 * @param metatableAccessor  metatable accessor to be used by this state, must not be
	 *                           {@code null}
	 *
	 * @throws NullPointerException  if {@code tableFactory} or {@code metatableAccessor}
	 *                               is {@code null}
	 */
	public LuaState(TableFactory tableFactory, MetatableAccessor metatableAccessor) {
		super(tableFactory, metatableAccessor);
	}

	/**
	 * Constructs a new {@code LuaState} with the default table factory and
	 * the default (empty) metatable accessor.
	 */
	public LuaState() {
		this(DefaultTable.factory(), new DefaultMetatableAccessor());
	}

	@Override
	public OneShotContinuation newCall(Object fn, Object... args) {
		return Call.init(this, fn, args).getCurrentContinuation();
	}

}
