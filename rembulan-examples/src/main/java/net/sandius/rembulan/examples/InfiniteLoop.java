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

package net.sandius.rembulan.examples;

import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.CompilerChunkLoader;
import net.sandius.rembulan.env.RuntimeEnvironments;
import net.sandius.rembulan.exec.CallException;
import net.sandius.rembulan.exec.CallPausedException;
import net.sandius.rembulan.exec.DirectCallExecutor;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.lib.impl.StandardLibrary;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.runtime.LuaFunction;

public class InfiniteLoop {

	public static void main(String[] args)
			throws InterruptedException, CallException, LoaderException {

		String program = "n = 0; while true do n = n + 1 end";

		StateContext state = StateContexts.newDefaultInstance();
		Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);

		ChunkLoader loader = CompilerChunkLoader.of("infinite_loop");
		LuaFunction main = loader.loadTextChunk(new Variable(env), "loop", program);

		// execute at most one million ops
		DirectCallExecutor executor = DirectCallExecutor.newExecutorWithTickLimit(1000000);

		try {
			executor.call(state, main);
			throw new AssertionError();  // never reaches this point
		}
		catch (CallPausedException ex) {
			System.out.println("n = " + env.rawget("n"));
		}

	}


}
