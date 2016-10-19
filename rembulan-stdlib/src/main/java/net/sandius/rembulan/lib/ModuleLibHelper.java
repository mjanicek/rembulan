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

import net.sandius.rembulan.Table;
import net.sandius.rembulan.runtime.LuaFunction;

public final class ModuleLibHelper {

	private ModuleLibHelper() {
		// not to be instantiated
	}

	public static LuaFunction getRequire(Table env) {
		Object req = env.rawget("require");
		return req instanceof LuaFunction ? (LuaFunction) req : null;
	}

	public static void addToPreLoad(Table env, String modName, LuaFunction loader) {
		Object pkg = env.rawget("package");
		if (pkg instanceof Table) {
			Object preload = ((Table) pkg).rawget("preload");
			if (preload instanceof Table) {
				((Table) preload).rawset(modName, loader);
			}
		}
	}

	public static void addToLoaded(Table env, String modName, Object value) {
		Object pkg = env.rawget("package");
		if (pkg instanceof Table) {
			Object loaded = ((Table) pkg).rawget("loaded");
			if (loaded instanceof Table) {
				((Table) loaded).rawset(modName, value);
			}
		}
	}

	public static void install(Table env, String modName, Object value) {
		env.rawset(modName, value);
		addToLoaded(env, modName, value);
	}

}
