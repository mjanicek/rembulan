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

import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.impl.SchedulingContexts;
import net.sandius.rembulan.runtime.AsyncTask;
import net.sandius.rembulan.runtime.RuntimeCallInitialiser;
import net.sandius.rembulan.runtime.SchedulingContext;
import net.sandius.rembulan.util.Check;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A call executor that executes Lua calls and asynchronous tasks scheduled by these
 * calls in the current thread.
 */
public class DirectCallExecutor {

	// Note: this API does not feel right: it conflates the instantiation of new calls
	// with the execution of arbitrary continuations, and does not offer a sufficiently
	// fine-grained control of scheduling contexts

	private final CallInitialiser initialiser;
	private final int cpuLimit;

	DirectCallExecutor(CallInitialiser initialiser, int cpuLimit) {
		this.initialiser = Objects.requireNonNull(initialiser);
		this.cpuLimit = cpuLimit;
	}

	/**
	 * Returns a new direct call executor that uses the given call initialiser to instantiate
	 * new calls. Calls executed by this executor will never be asked to pause by the
	 * scheduler.
	 *
	 * @param initialiser  the call initialiser used by the executor, must not be {@code null}
	 * @return  a direct call executor that never asks continuations to be paused
	 *
	 * @throws NullPointerException  if {@code initialiser} is {@code null}
	 */
	public static DirectCallExecutor newExecutor(CallInitialiser initialiser) {
		return new DirectCallExecutor(initialiser, -1);
	}

	/**
	 * Returns a new direct call executor that uses the runtime call initialiser
	 * to instantiate new calls in the state context {@code stateContext}.
	 * Calls executed by this executor will never be asked to pause by the
	 * scheduler.
	 *
	 * @param stateContext  the state context to initialise calls in, must not be {@code null}
	 * @return  a direct call executor that never asks continuations to be paused
	 *
	 * @throws NullPointerException  if {@code stateContext} is {@code null}
	 */
	public static DirectCallExecutor newExecutor(StateContext stateContext) {
		return newExecutor(RuntimeCallInitialiser.forState(stateContext));
	}

	/**
	 * Returns a new direct call executor that uses the given call initialiser to instantiate
	 * new calls, and with every {@link #resume(Continuation)} capped at {@code ticksLimit} ticks.
	 *
	 * @param initialiser  the call initialiser used by the executor, must not be {@code null}
	 * @param ticksLimit  the tick limit for resumes, must be positive
	 * @return   a direct call executor that caps resumes at the given tick limit
	 *
	 * @throws NullPointerException  if {@code initialiser} is {@code null}
	 * @throws IllegalArgumentException  if {@code ticksLimit} is not positive
	 */
	public static DirectCallExecutor newExecutorWithCpuLimit(CallInitialiser initialiser, int ticksLimit) {
		return new DirectCallExecutor(initialiser, Check.positive(ticksLimit));
	}

	/**
	 * Returns a new direct call executor that uses the runtime call initialiser to instantiate
	 * new calls in the state context {@code stateContext}, and that limits every
	 * {@link #resume(Continuation)} to {@code ticksLimit} ticks.
	 *
	 * @param stateContext  the state context used to initialise new calls, must not be {@code null}
	 * @param ticksLimit  the tick limit for resumes, must be positive
	 * @return   a direct call executor that caps resumes at the given tick limit
	 *
	 * @throws NullPointerException  if {@code stateContext} is {@code null}
	 * @throws IllegalArgumentException  if {@code ticksLimit} is not positive
	 */
	public static DirectCallExecutor newExecutorWithCpuLimit(StateContext stateContext, int ticksLimit) {
		return newExecutorWithCpuLimit(RuntimeCallInitialiser.forState(stateContext), ticksLimit);
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

	/**
	 * Calls {@code fn(args...)} in the current thread, returning the call result once
	 * the call completes.
	 *
	 * <p>The call result will be passed in a freshly-allocated array, and may therefore
	 * be manipulated freely by the caller of this method.</p>
	 *
	 * @param fn  the call target, may be {@code null}
	 * @param args  call arguments, must not be {@code null}
	 * @return  the call result
	 *
	 * @throws CallException  if the call terminated abnormally
	 * @throws CallInterruptedException  if the call initiated a pause
	 * @throws InterruptedException  when the current thread is interrupted while waiting
	 *                               for an asynchronous operation to be completed
	 * @throws NullPointerException  if {@code args} is {@code null}
	 */
	public Object[] call(Object fn, Object... args)
			throws CallException, CallInterruptedException, InterruptedException {

		return resume(initialiser.newCall(fn, args));
	}

	/**
	 * Resumes {@code continuation} in the current thread, returning the call result once
	 * the call is completes.
	 *
	 * <p>The call result will be passed in a freshly-allocated array, and may therefore
	 * be manipulated freely by the caller of this method.</p>
	 *
	 * @param continuation  the continuation to resume, must not be {@code null}
	 * @return  the call result
	 *
	 * @throws CallException  if the call terminated abnormally
	 * @throws CallInterruptedException  if the call initiated a pause
	 * @throws InterruptedException  when the current thread is interrupted while waiting
	 *                               for an asynchronous operation to be completed
	 * @throws InvalidContinuationException  when {@code continuation} is invalid
	 * @throws NullPointerException  if {@code args} is {@code null}
	 */
	public Object[] resume(Continuation continuation)
			throws CallException, CallInterruptedException, InterruptedException {

		Objects.requireNonNull(continuation);

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
