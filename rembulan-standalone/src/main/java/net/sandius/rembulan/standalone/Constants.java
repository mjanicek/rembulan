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

package net.sandius.rembulan.standalone;

abstract class Constants {

	/**
	 * Default prompt for the first input line in interactive mode.
	 */
	public static final String DEFAULT_PROMPT = "> ";

	/**
	 * Default prompt for multi-line inputs in interactive mode.
	 */
	public static final String DEFAULT_PROMPT2 = ">> ";


	/**
	 * Variable name used to look up the first input line prompt in interactive mode.
	 */
	public static final String VAR_NAME_PROMPT = "_PROMPT";

	/**
	 * Variable name used to look up the prompt for multi-line inputs in interactive mode.
	 */
	public static final String VAR_NAME_PROMPT2 = "_PROMPT2";

	/**
	 * Name of an environment variable containing an init script to be executed before
	 * any other Lua code when defined.
	 *
	 * <p>This is the first such variable to be tested; when not defined,
	 * {@link #ENV_INIT_SECOND} will be tried.</p>
	 */
	public static final String ENV_INIT_FIRST = "LUA_INIT_5_3";

	/**
	 * Name of an environment variable containing an init script to be executed before
	 * any other Lua code when defined.
	 *
	 * <p>This environment variable is tried only when {@link #ENV_INIT_FIRST}
	 * is not defined.</p>
	 */
	public static final String ENV_INIT_SECOND = "LUA_INIT";

	/**
	 * Name of the environment variable controlling the stack traceback format.
	 *
	 * <p>When the variable is defined, the console will print the full traceback
	 * on errors; otherwise, the traceback will be filtered for better readability.</p>
	 */
	public static final String ENV_FULL_TRACEBACK = "REMBULAN_FULL_TRACEBACK";

	/**
	 * File name used for chunks read from the standard input.
	 */
	public static final String SOURCE_STDIN = "stdin";

	/**
	 * File name used for chunks read from the command line.
	 */
	public static final String SOURCE_COMMAND_LINE = "(command line)";

	private Constants() {
		// not to be instantiated or extended
	}

}
