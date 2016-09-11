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
import net.sandius.rembulan.exec.CallEventHandler;
import net.sandius.rembulan.exec.CallException;
import net.sandius.rembulan.exec.CallPausedException;
import net.sandius.rembulan.exec.Continuation;
import net.sandius.rembulan.impl.SchedulingContexts;
import net.sandius.rembulan.impl.StateContexts;
import net.sandius.rembulan.load.LoaderException;
import net.sandius.rembulan.runtime.AbstractFunction2;
import net.sandius.rembulan.runtime.AsyncTask;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.RuntimeCallInitialiser;
import net.sandius.rembulan.runtime.UnresolvedControlThrowable;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleMultiThreadedAsync {

	public static final int INITIAL_POPULATION = 10;
	public static final double SPAWN_CHANCE = 0.8;

	private static final AtomicInteger nextId = new AtomicInteger(1);
	private static final AtomicInteger numLive = new AtomicInteger(0);

	private static final AsyncFunction FN_INSTANCE = new AsyncFunction();

	static class SleepAndSpawn implements AsyncTask {

		private final Object id;
		private final AtomicInteger var;
		private final StateContext context;
		private final BlockingQueue<Continuation> workQueue;

		SleepAndSpawn(Object id, AtomicInteger var, StateContext context, BlockingQueue<Continuation> workQueue) {
			this.id = id;
			this.var = var;
			this.context = Objects.requireNonNull(context);
			this.workQueue = Objects.requireNonNull(workQueue);
		}

		@Override
		public void execute(ContinueCallback callback) {
			int sleepMs = ThreadLocalRandom.current().nextInt(1, 100);
			System.out.println("[" + Thread.currentThread() + "]: BEGIN async task of " + id
					+ " (will sleep for " + sleepMs + " ms)");
			try {
				Thread.sleep(sleepMs);
				var.set(sleepMs);

				if (ThreadLocalRandom.current().nextDouble() <= SPAWN_CHANCE) {
					// spawn a new task!
					spawnNewTask(context, workQueue);
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			finally {
				System.out.println("[" + Thread.currentThread() + "]: END async task of " + id);
				callback.finished();
			}
		}
	}

	static class AsyncFunction extends AbstractFunction2 {

		@Override
		public void invoke(ExecutionContext context, Object id, Object arg2) throws ResolvedControlThrowable {

			@SuppressWarnings("unchecked")
			final BlockingQueue<Continuation> workQueue = (BlockingQueue<Continuation>) arg2;

			System.out.println("[" + Thread.currentThread() + "]: Invoke " + id);

			AtomicInteger var = new AtomicInteger(0);

			try {
				context.resumeAfter(new SleepAndSpawn(id, var, context, workQueue));
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, new Object[] {id, var});
			}

			throw new AssertionError();  // control never reaches this point
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			Object[] a = (Object[]) suspendedState;
			Object id = a[0];
			AtomicInteger var = (AtomicInteger) a[1];
			System.out.println("[" + Thread.currentThread() + "]: Resume " + id);
			context.getReturnBuffer().setTo(var.get());
		}

	}

	private static void spawnNewTask(StateContext context, BlockingQueue<Continuation> workQueue) {
		Continuation cont = RuntimeCallInitialiser.forState(context).newCall(FN_INSTANCE, nextId.getAndIncrement(), workQueue);
		assert (cont != null);
		numLive.incrementAndGet();
		workQueue.add(cont);
	}

	static void executeInNewThread(final AsyncTask task, final AsyncTask.ContinueCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				task.execute(callback);
			}
		}).start();
	}

	public static void main(String[] args)
			throws InterruptedException, CallPausedException, CallException, LoaderException {

		StateContext state = StateContexts.newDefaultInstance();

		final LinkedBlockingQueue<Continuation> workQueue = new LinkedBlockingQueue<>();

		final AtomicInteger sum = new AtomicInteger(0);

		CallEventHandler handler = new CallEventHandler() {
			@Override
			public void returned(Object id, Object[] result) {
				sum.getAndAdd((Integer) result[0]);
				numLive.decrementAndGet();
			}

			@Override
			public void failed(Object id, Throwable error) {
				numLive.decrementAndGet();
			}

			@Override
			public void paused(Object id, final Continuation c) {
				workQueue.add(c);
			}

			@Override
			public void async(Object id, final Continuation c, final AsyncTask task) {
				executeInNewThread(task, new AsyncTask.ContinueCallback() {
					@Override
					public void finished() {
						workQueue.add(c);
					}
				});
			}
		};

		long before = System.nanoTime();

		for (int i = 0; i < INITIAL_POPULATION; i++) {
			spawnNewTask(state, workQueue);
		}

		while (numLive.get() > 0) {
			Continuation cont = workQueue.take();
			cont.resume(handler, SchedulingContexts.neverPause());
		}

		long after = System.nanoTime();

		System.out.println();
		System.out.println("Total time spent: " + String.format("%.1f ms", (after - before) / 1000000.0));
		System.out.println("Total sleep time: " + sum.get() + " ms");

	}

}
