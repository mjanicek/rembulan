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

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.impl.ReturnBuffers;
import net.sandius.rembulan.impl.SchedulingContexts;
import net.sandius.rembulan.runtime.AsyncTask;
import net.sandius.rembulan.runtime.ReturnBufferFactory;
import net.sandius.rembulan.runtime.RuntimeCallInitialiser;
import net.sandius.rembulan.runtime.SchedulingContext;
import net.sandius.rembulan.runtime.SchedulingContextFactory;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A call executor that executes Lua calls and asynchronous tasks scheduled by these
 * calls in the current thread. The executor uses a {@link SchedulingContextFactory}
 * to instantiate scheduling contexts.
 */
public class DirectCallExecutor {

	private final SchedulingContextFactory schedulingContextFactory;
	private final ReturnBufferFactory returnBufferFactory;
	private final boolean performJavaConversions;

	DirectCallExecutor(SchedulingContextFactory schedulingContextFactory) {
		this.schedulingContextFactory = Objects.requireNonNull(schedulingContextFactory);
		this.returnBufferFactory = DEFAULT_RETURN_BUFFER_FACTORY;
		this.performJavaConversions = true;
	}

	private static final ReturnBufferFactory DEFAULT_RETURN_BUFFER_FACTORY =
			ReturnBuffers.defaultFactory();

	private static final DirectCallExecutor NEVER_PAUSING_EXECUTOR
			= new DirectCallExecutor(SchedulingContexts.neverPauseFactory());

	/**
	 * Returns a new direct call executor with a scheduler that never requests executions
	 * to be paused.
	 *
	 * @return  a direct call executor that never asks continuations to be paused
	 */
	public static DirectCallExecutor newExecutor() {
		return NEVER_PAUSING_EXECUTOR;
	}

	/**
	 * Returns a new direct call executor with the specified scheduling context factory used
	 * to instantiate a new scheduling context on each resume.
	 *
	 * @param schedulingContextFactory  the scheduling context factory, must not be {@code null}
	 * @return  a direct call executor that uses the specified scheduling context factory
	 *
	 * @throws NullPointerException  if {@code schedulingContextFactory} is {@code null}
	 */
	public static DirectCallExecutor newExecutor(SchedulingContextFactory schedulingContextFactory) {
		return new DirectCallExecutor(schedulingContextFactory);
	}

	/**
	 * Returns a new direct call executor that uses that asks each continuation it resumes
	 * to pause after it has registered {@code ticksLimit} ticks.
	 *
	 * @param ticksLimit  the tick limit for resumes, must be positive
	 * @return   a direct call executor that caps resumes at the given tick limit
	 *
	 * @throws IllegalArgumentException  if {@code ticksLimit} is not positive
	 */
	public static DirectCallExecutor newExecutorWithTickLimit(long ticksLimit) {
		return newExecutor(SchedulingContexts.countDownContextFactory(ticksLimit));
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
				throws CallException, CallPausedException {

			if (!wasSet.get()) {
				throw new IllegalStateException("Call result has not been set");
			}
			else {
				if (values != null) return values;
				else if (cont != null) throw new CallPausedException(cont);
				else if (error != null) throw new CallException(error);
				else {
					// should not happen
					throw new AssertionError();
				}
			}
		}

	}

	/**
	 * Returns the scheduling context factory used by this executor.
	 *
	 * @return  the scheduling context factory used by this executor
	 */
	public SchedulingContextFactory schedulingContextFactory() {
		return schedulingContextFactory;
	}

	/**
	 * Calls {@code fn(args...)} in the current thread in the state context {@code stateContext},
	 * returning the call result once the call completes.
	 *
	 * <p>The call result will be passed in a freshly-allocated array, and may therefore
	 * be manipulated freely by the caller of this method.</p>
	 *
	 * @param stateContext  state context of the call, must not be {@code null}
	 * @param fn  the call target, may be {@code null}
	 * @param args  call arguments, must not be {@code null}
	 * @return  the call result
	 *
	 * @throws CallException  if the call terminated abnormally
	 * @throws CallPausedException  if the call initiated a pause
	 * @throws InterruptedException  when the current thread is interrupted while waiting
	 *                               for an asynchronous operation to be completed
	 * @throws NullPointerException  if {@code stateContext} or {@code args} is {@code null}
	 */
	public Object[] call(StateContext stateContext, Object fn, Object... args)
			throws CallException, CallPausedException, InterruptedException {

		CallInitialiser initialiser = RuntimeCallInitialiser.forState(
				stateContext,
				returnBufferFactory);

		return resume(initialiser.newCall(
				performJavaConversions ? Conversions.canonicalRepresentationOf(fn) : fn,
				performJavaConversions ? Conversions.copyAsCanonicalValues(args) : args));
	}

	/**
	 * Resumes {@code continuation} in the current thread, returning the call result once
	 * the call completes.
	 *
	 * <p>The call result will be passed in a freshly-allocated array, and may therefore
	 * be manipulated freely by the caller of this method.</p>
	 *
	 * @param continuation  the continuation to resume, must not be {@code null}
	 * @return  the call result
	 *
	 * @throws CallException  if the call terminated abnormally
	 * @throws CallPausedException  if the call initiated a pause
	 * @throws InterruptedException  when the current thread is interrupted while waiting
	 *                               for an asynchronous operation to be completed
	 * @throws InvalidContinuationException  when {@code continuation} is invalid
	 * @throws NullPointerException  if {@code continuation} is {@code null}
	 */
	public Object[] resume(Continuation continuation)
			throws CallException, CallPausedException, InterruptedException {
		return execute(continuation, schedulingContextFactory.newInstance(), performJavaConversions);
	}

	/**
	 * Resumes {@code continuation} in the current thread in the scheduling context
	 * {@code schedulingContext}, returning the call result once the call completes.
	 *
	 * <p>The call result will be passed in a freshly-allocated array, and may therefore
	 * be manipulated freely by the caller of this method. If {@code convertResultsToJava}
	 * is {@code true}, the result values will be converted to Java using
	 * {@link Conversions#toJavaValues(Object[])}.</p>
	 *
	 * @param continuation  the continuation to resume, must not be {@code null}
	 * @param schedulingContext  the scheduling context, must not be {@code null}
	 * @param convertResultsToJava  flag controlling the conversion of result values to
	 *                              their Java representations
	 * @return  the call result, converted to Java representations
	 *          if {@code convertResults} is {@code true}
	 *
	 * @throws CallException  if the call terminated abnormally
	 * @throws CallPausedException  if the call initiated a pause
	 * @throws InterruptedException  when the current thread is interrupted while waiting
	 *                               for an asynchronous operation to be completed
	 * @throws InvalidContinuationException  when {@code continuation} is invalid
	 * @throws NullPointerException  if {@code continuation} or {@code schedulingContext}
	 *                               is {@code null}
	 */
	public static Object[] execute(
			Continuation continuation,
			SchedulingContext schedulingContext,
			boolean convertResultsToJava)
			throws CallException, CallPausedException, InterruptedException {

		Objects.requireNonNull(continuation);
		Objects.requireNonNull(schedulingContext);

		while (true) {
			Result result = new Result();
			continuation.resume(result, schedulingContext);

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
				Object[] values = result.get();
				if (convertResultsToJava) {
					Conversions.toJavaValues(values);
				}

				return values;
			}
		}
	}

	/**
	 * Resumes {@code continuation} in the current thread in the scheduling context
	 * {@code schedulingContext}, returning the call result once the call completes.
	 *
	 * <p>The call result will be passed in a freshly-allocated array, and may therefore
	 * be manipulated freely by the caller of this method.</p>
	 *
	 * <p>This method converts return values to Java values using
	 * {@link Conversions#toJavaValues(Object[])}. For a greater control over this behaviour,
	 * use {@link #execute(Continuation, SchedulingContext, boolean)} instead.</p>
	 *
	 * @param continuation  the continuation to resume, must not be {@code null}
	 * @param schedulingContext  the scheduling context, must not be {@code null}
	 * @return  the call result
	 *
	 * @throws CallException  if the call terminated abnormally
	 * @throws CallPausedException  if the call initiated a pause
	 * @throws InterruptedException  when the current thread is interrupted while waiting
	 *                               for an asynchronous operation to be completed
	 * @throws InvalidContinuationException  when {@code continuation} is invalid
	 * @throws NullPointerException  if {@code continuation} or {@code schedulingContext}
	 *                               is {@code null}
	 */
	public static Object[] execute(
			Continuation continuation,
			SchedulingContext schedulingContext)
			throws CallException, CallPausedException, InterruptedException {

		return execute(continuation, schedulingContext, true);
	}

	/**
	 * Executes {@code continuation} in the current thread in a scheduling context
	 * that never asks the execution to pause, returning the call result once the call completes.
	 *
	 * <p>The call result will be passed in a freshly-allocated array, and may therefore
	 * be manipulated freely by the caller of this method.</p>
	 *
	 * <p>This method converts return values to Java values using
	 * {@link Conversions#toJavaValues(Object[])}. For a greater control over this behaviour,
	 * use {@link #execute(Continuation, SchedulingContext, boolean)} instead.</p>
	 *
	 * @param continuation  the continuation to resume, must not be {@code null}
	 * @return  the call result
	 *
	 * @throws CallException  if the call terminated abnormally
	 * @throws CallPausedException  if the call initiated a pause
	 * @throws InterruptedException  when the current thread is interrupted while waiting
	 *                               for an asynchronous operation to be completed
	 * @throws InvalidContinuationException  when {@code continuation} is invalid
	 * @throws NullPointerException  if {@code continuation} or {@code schedulingContext}
	 *                               is {@code null}
	 */
	public static Object[] execute(Continuation continuation)
			throws CallException, CallPausedException, InterruptedException {
		return execute(continuation, SchedulingContexts.neverPause());
	}

}
