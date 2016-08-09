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

package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class CallMultiplexer {

	private final ExecutorService executorService;
	private final MultiplexCallEventHandler handler;
	private final PreemptionHandler preemptionHandler;

	private final LinkedBlockingQueue<CountDownLatch> latches;
	private final Map<Call, CountDownLatch> calls;

	public CallMultiplexer(ExecutorService executorService, PreemptionHandler preemptionHandler) {
		this.executorService = Check.notNull(executorService);
		this.handler = new MultiplexCallEventHandler();
		this.preemptionHandler = Check.notNull(preemptionHandler);

		this.latches = new LinkedBlockingQueue<>();
		this.calls = new HashMap<>();
	}

	public Future<Object[]> submitCall(Call c, PreemptionContext preemptionContext) {
		CountDownLatch latch = new CountDownLatch(1);
		latches.add(latch);
		synchronized (calls) {
			calls.put(c, latch);
		}

		executorService.submit(c.currentContinuation().toCallable(handler, preemptionContext));
		return c.result();
	}

	@Deprecated
	public Future<Object[]> submitCall(LuaState state, PreemptionContext preemptionContext, Object fn, Object... args) {
		return submitCall(Call.init(state, fn, args), preemptionContext);
	}

	private void done(Call c) {
		final CountDownLatch latch;
		synchronized (calls) {
			latch = calls.remove(c);
		}
		if (latch != null) {
			latch.countDown();
		}
	}

	public void awaitAll() throws InterruptedException {
		CountDownLatch c;
		do {
			c = latches.poll();
			if (c != null) {
				c.await();
			}
		} while (c != null);
	}

	public void shutdown() {
		executorService.shutdown();
	}

	public interface PreemptionHandler {

		boolean preempted(Call.Continuation c, PreemptionContext preemptionContext);

	}

	private class MultiplexCallEventHandler implements Call.EventHandler {

		@Override
		public void paused(Call c, Call.Continuation cont, PreemptionContext preemptionContext) {
			if (preemptionHandler.preempted(cont, preemptionContext)) {
				final Callable<Object[]> cc = cont.toCallable(handler, preemptionContext);
				executorService.submit(cc);
			}
		}

		@Override
		public void waiting(final Call c, final Runnable task, Call.Continuation cont, PreemptionContext preemptionContext) {
			final Callable<Object[]> cc = cont.toCallable(handler, preemptionContext);
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						task.run();
					}
					finally {
						executorService.submit(cc);
					}
				}
			});
		}

		@Override
		public void returned(Call c, Object[] result) {
			done(c);
		}

		@Override
		public void failed(Call c, Throwable error) {
			done(c);
		}
	}

}
