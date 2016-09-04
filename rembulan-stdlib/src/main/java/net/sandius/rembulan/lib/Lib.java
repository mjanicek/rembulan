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

package net.sandius.rembulan.lib;

import net.sandius.rembulan.LuaState;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.TableFactory;

public abstract class Lib {

	public static final String MT_NAME = "__name";
	public static final String TYPENAME_LIGHT_USERDATA = "light userdata";

	public abstract String name();

	public abstract Table toTable(TableFactory tableFactory);

	public void preInstall(LuaState state, Table env) {
		// no-op by default
	}

	@Deprecated
	public void installInto(LuaState state, Table env) {
		preInstall(state, env);

		Table t = toTable(state.tableFactory());
		if (t != null) {
			env.rawset(name(), t);
		}

		postInstall(state, env, t);
	}

	public void postInstall(LuaState state, Table env, Table libTable) {
		// no-op by default
	}

}
