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

package net.sandius.rembulan.exec;

import net.sandius.rembulan.AsyncTask;
import net.sandius.rembulan.SchedulingContext;
import net.sandius.rembulan.impl.SchedulingContexts;
import net.sandius.rembulan.util.Check;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class DirectCallExecutor {

	private final CallInitialiser initialiser;
	private final int cpuLimit;

	DirectCallExecutor(CallInitialiser initialiser, int cpuLimit) {
		this.initialiser = Objects.requireNonNull(initialiser);
		this.cpuLimit = cpuLimit;
	}

	public static DirectCallExecutor newExecutor(CallInitialiser initialiser) {
		return new DirectCallExecutor(initialiser, -1);
	}

	public static DirectCallExecutor newExecutorWithCpuLimit(CallInitialiser initialiser, int cpuLimit) {
		return new DirectCallExecutor(initialiser, Check.positive(cpuLimit));
	}

	private static class Result implements CallEventHandler {

		private final AtomicBoolean wasSet;

		// if wasSet.get() == true, then at most one of the next three fields may be null;
		// otherwise, all must be null.

		private Continuation cont;
		private Object[] values;
		private Throwable error;

		// may only be non-null if wasSet.get() == true and cont != null
		private AsyncTask task;

		Result() {
			this.wasSet = new AtomicBoolean(false);
			this.cont = null;
			this.values = null;
			this.error = null;
			this.task = null;
		}

		@Override
		public void returned(Object id, Object[] result) {
			if (result != null) {
				if (wasSet.compareAndSet(false, true)) {
					this.values = result;
				}
				else {
					throw new IllegalStateException("Call result already set");
				}
			}
			else {
				throw new IllegalArgumentException("Return values array must not be null");
			}
		}

		@Override
		public void failed(Object id, Throwable error) {
			if (error != null) {
				if (wasSet.compareAndSet(false, true)) {
					this.error = error;
				}
				else {
					throw new IllegalStateException("Call result already set");
				}
			}
			else {
				throw new IllegalArgumentException("Error must not be null");
			}
		}

		@Override
		public void paused(Object id, Continuation cont) {
			if (cont != null) {
				if (wasSet.compareAndSet(false, true)) {
					this.cont = cont;
				}
				else {
					throw new IllegalStateException("Call result already set");
				}
			}
			else {
				throw new IllegalArgumentException("Continuation must not be null");
			}
		}

		@Override
		public void async(Object id, final Continuation cont, AsyncTask task) {
			if (cont != null && task != null) {
				if (wasSet.compareAndSet(false, true)) {
					this.cont = cont;
					this.task = task;
				}
				else {
					throw new IllegalStateException("Call result already set");
				}
			}
			else {
				throw new IllegalArgumentException("Continuation and task must not be null");
			}
		}

		public Object[] get()
				throws CallException, CallInterruptedException {

			if (!wasSet.get()) {
				throw new IllegalStateException("Call result has not been set");
			}
			else {
				if (values != null) return values;
				else if (cont != null) throw new CallInterruptedException(cont);
				else if (error != null) throw new CallException(error);
				else {
					// should not happen
					throw new AssertionError();
				}
			}
		}

	}

	private SchedulingContext preemptionContext() {
		return cpuLimit > 0 ? SchedulingContexts.upTo(cpuLimit) : SchedulingContexts.never();
	}

	public Object[] call(Object fn, Object... args)
			throws CallException, CallInterruptedException, InterruptedException {

		return resume(initialiser.newCall(fn, args));
	}

	public Object[] resume(Continuation continuation)
			throws CallException, CallInterruptedException, InterruptedException {

		while (true) {
			Result result = new Result();
			continuation.resume(result, preemptionContext());

			if (result.wasSet.get() && result.task != null && result.cont != null) {
				// an asynchronous task

				final CountDownLatch latch = new CountDownLatch(1);
				AsyncTask.ContinueCallback callback = new AsyncTask.ContinueCallback() {
					@Override
					public void finished() {
						latch.countDown();
					}
				};

				continuation = result.cont;
				result.task.execute(callback);

				// TODO: handle interrupts while waiting, and give the user a chance to try again?
				latch.await();
			}
			else {
				return result.get();
			}
		}

	}

}
