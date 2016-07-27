package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class CallMultiplexer {

	private final ExecutorService executorService;
	private final MultiplexCallEventHandler handler;

	private final LinkedBlockingQueue<CountDownLatch> latches;
	private final Map<Call, CountDownLatch> calls;

	public CallMultiplexer(ExecutorService executorService) {
		this.executorService = Check.notNull(executorService);
		this.handler = new MultiplexCallEventHandler();

		this.latches = new LinkedBlockingQueue<>();
		this.calls = new HashMap<>();
	}

	public Future<Object[]> submitCall(Call c) {
		CountDownLatch latch = new CountDownLatch(1);
		latches.add(latch);
		synchronized (calls) {
			calls.put(c, latch);
		}

		executorService.submit(c.currentContinuationCallable());
		return c.result();
	}

	public Future<Object[]> submitCall(LuaState state, PreemptionContext preemptionContext, Object fn, Object... args) {
		return submitCall(Call.init(state, preemptionContext, handler, fn, args));
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

	private class MultiplexCallEventHandler implements Call.EventHandler {

		@Override
		public void paused(Call c, Call.Continuation cont) {
			// ignore pauses
		}

		@Override
		public void waiting(final Call c, final Runnable task, final Call.Continuation cont) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						task.run();
					}
					finally {
						executorService.submit(cont);
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
