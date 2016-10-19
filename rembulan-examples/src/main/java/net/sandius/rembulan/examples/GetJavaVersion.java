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
import net.sandius.rembulan.impl.NonsuspendableFunctionException;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.lib.StandardLibrary;
import net.sandius.rembulan.load.ChunkLoader;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.runtime.AbstractFunction0;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.util.Arrays;

public class GetJavaVersion {

	static class JavaVersion extends AbstractFunction0 {

		@Override
		public void invoke(ExecutionContext context) throws ResolvedControlThrowable {
			String javaVmName = System.getProperty("java.vm.name");
			String javaVersion = System.getProperty("java.version");
			context.getReturnBuffer().setTo(javaVmName, javaVersion);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			throw new NonsuspendableFunctionException();
		}

	}


	public static void main(String[] args)
			throws InterruptedException, CallPausedException, CallException, LoaderException {

		String program = "local vmname, version = javaversion()\n"
				+ "return 'Java VM name = \"'..vmname..'\", Java version = \"'..version..'\"'";

		StateContext state = StateContexts.newDefaultInstance();
		Table env = StandardLibrary.in(RuntimeEnvironments.system()).installInto(state);
		env.rawset("javaversion", new JavaVersion());

		ChunkLoader loader = CompilerChunkLoader.of("call_from_lua");
		LuaFunction main = loader.loadTextChunk(new Variable(env), "", program);

		Object[] result = DirectCallExecutor.newExecutor().call(state, main);

		System.out.println("Result: " + Arrays.toString(result));

	}

}
