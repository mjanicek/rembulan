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

package net.sandius.rembulan.core.exec;

import net.sandius.rembulan.core.Call;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.util.Check;

import java.util.concurrent.Callable;

public class DirectCallExecutor {

	private final LuaState state;
	private final int cpuLimit;

	DirectCallExecutor(LuaState state, int cpuLimit) {
		this.state = Check.notNull(state);
		this.cpuLimit = cpuLimit;
	}

	public static DirectCallExecutor newExecutor(LuaState state) {
		return new DirectCallExecutor(state, -1);
	}

	public static DirectCallExecutor newExecutorWithCpuLimit(LuaState state, int cpuLimit) {
		return new DirectCallExecutor(state, Check.positive(cpuLimit));
	}

	private static class CountingPreemptionContext implements PreemptionContext {

		private int allowance;

		CountingPreemptionContext(int allowance) {
			this.allowance = allowance;
		}

		@Override
		public boolean withdraw(int cost) {
			if (cost > 0) {
				allowance -= cost;
				return allowance <= 0;
			}
			else {
				return false;
			}
		}

		@Override
		public boolean isPreempted() {
			return allowance <= 0;
		}
	}

	private PreemptionContext preemptionContext() {
		return cpuLimit > 0 ? new CountingPreemptionContext(cpuLimit) : PreemptionContext.Never.INSTANCE;
	}

	private static class Handler implements Call.EventHandler {

		private Call.Continuation continuation;

		Handler() {
			this.continuation = null;
		}

		@Override
		public void paused(Call c, Call.Continuation cont, PreemptionContext preemptionContext) {
			continuation = cont;
		}

		@Override
		public void waiting(Call c, Runnable task, Call.Continuation cont, PreemptionContext preemptionContext) {
			throw new UnsupportedOperationException("Waiting not supported");
		}

		@Override
		public void returned(Call c, Object[] result) {
			// no-op
		}

		@Override
		public void failed(Call c, Throwable error) {
			// no-op
		}
	}

	public Object[] call(Object fn, Object... args)
			throws CallException, CallInterruptedException {

		Call call = Call.init(state, fn, args);
		return resume(call.currentContinuation());
	}

	public Object[] resume(Call.Continuation continuation)
			throws CallException, CallInterruptedException {

		Handler handler = new Handler();

		Callable<Object[]> task = continuation.toCallable(handler, preemptionContext());
		Object[] result = null;
		try {
			result = task.call();
		}
		catch (Exception ex) {
			throw new CallException(ex);
		}

		if (result == null) {
			Call.Continuation cont = handler.continuation;
			if (cont != null) {
				throw new CallInterruptedException(cont);
			}
			else {
				throw new IllegalStateException("Call paused, but no continuation given");
			}
		}
		else {
			return result;
		}
	}

}
