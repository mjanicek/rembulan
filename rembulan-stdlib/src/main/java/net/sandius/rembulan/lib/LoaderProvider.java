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
import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.runtime.LuaFunction;

/**
 * A provider of library loaders.
 */
public interface LoaderProvider {

	/**
	 * Returns the name of the library provided by this provider.
	 *
	 * @return  the name of the library
	 */
	String name();

	/**
	 * Returns a new instance of the loader function for this library.
	 *
	 * @param runtimeEnvironment  the runtime environment to be used by the provider
	 * @param env  the global {@code _ENV} table used to load the library
	 *
	 * @return  a new instance of the library loader
	 */
	LuaFunction newLoader(RuntimeEnvironment runtimeEnvironment, Table env);

}
