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

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Call {

	private final LuaState state;
	private final ObjectSink objectSink;

	private Coroutine currentCoroutine;

	private final int startingVersion;
	private final AtomicInteger currentVersion;

	private static final int VERSION_RUNNING = 0;
	private static final int VERSION_TERMINATED = 1;
	private static final int VERSION_CANCELLED = 2;

	private Call(
			LuaState state,
			ObjectSink objectSink,
			Coroutine mainCoroutine) {

		this.state = Check.notNull(state);
		this.objectSink = Check.notNull(objectSink);

		this.currentCoroutine = Check.notNull(mainCoroutine);

		this.startingVersion = newPausedVersion(0);
		this.currentVersion = new AtomicInteger(startingVersion);
	}

	public static Call init(
			LuaState state,
			Object fn,
			Object... args) {

		ObjectSink objectSink = state.newObjectSink();
		Coroutine c = new Coroutine(fn);
		objectSink.setToArray(args);
		return new Call(state, objectSink, c);
	}

	public enum State {
		PAUSED,
		RUNNING,
		TERMINATED,
		CANCELLED
	}

	private int newPausedVersion(int oldVersion) {
		int v;
		Random versionSource = ThreadLocalRandom.current();
		do {
			v = versionSource.nextInt();
		} while (!isPaused(v) || v == oldVersion);
		return v;
	}

	private static boolean isPaused(int version) {
		return version != VERSION_RUNNING
				&& version != VERSION_TERMINATED
				&& version != VERSION_CANCELLED;
	}

	private static State versionToState(int version) {
		switch (version) {
			case VERSION_RUNNING:    return State.RUNNING;
			case VERSION_TERMINATED: return State.TERMINATED;
			case VERSION_CANCELLED:  return State.CANCELLED;
			default:                 return State.PAUSED;
		}
	}

	public State state() {
		return versionToState(currentVersion.get());
	}

	// cancel this call if it hasn't started yet
	// return true if successful, false if the call has already been started,
	// or the call is already cancelled  -- FIXME: that's not very intuitive
	private boolean cancelIfNotStarted() {
		if (currentVersion.compareAndSet(startingVersion, VERSION_CANCELLED)) {
			// not started yet
			return true;
		}
		else {
			return false;
		}
	}

	private boolean interruptIfRunning() {
		// TODO
		return false;
	}

	private class ResultFuture implements Future<Object[]> {

		private Object[] result;
		private Throwable error;
		private final CountDownLatch latch;

		private final AtomicBoolean completed;

		public ResultFuture() {
			this.result = null;
			this.error = null;

			this.latch = new CountDownLatch(1);
			this.completed = new AtomicBoolean(false);
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (completed.compareAndSet(false, true)) {
				boolean result = cancelIfNotStarted()
						|| (mayInterruptIfRunning && interruptIfRunning());
				latch.countDown();
				return false;
			}
			else {
				return true;
			}
		}

		public void complete(Object[] result) {
			Check.notNull(result);
			if (completed.compareAndSet(false, true)) {
				this.result = result;
				latch.countDown();
			}
			else {
				throw new IllegalStateException("Future already completed");
			}
		}

		public void fail(Throwable error) {
			Check.notNull(error);
			if (completed.compareAndSet(false, true)) {
				this.error = error;
				latch.countDown();
			}
			else {
				throw new IllegalStateException("Future already completed");
			}
		}


		@Override
		public boolean isCancelled() {
			return isDone() && result == null && error == null;
		}

		@Override
		public boolean isDone() {
			return latch.getCount() == 0;
		}

		private Object[] getResult() throws ExecutionException {
			if (result != null) {
				return result;
			}
			else if (error != null) {
				throw new ExecutionException(error);
			}
			else {
				throw new CancellationException();
			}
		}

		@Override
		public Object[] get() throws InterruptedException, ExecutionException {
			latch.await();
			return getResult();
		}

		@Override
		public Object[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			if (latch.await(timeout, unit)) {
				return getResult();
			}
			else {
				throw new TimeoutException();
			}
		}

	};

	private final ResultFuture result = new ResultFuture();

	public Future<Object[]> result() {
		return result;
	}

	protected static class Context implements ExecutionContext {

		private final Call call;
		private final PreemptionContext preemptionContext;

		public Context(Call call, PreemptionContext preemptionContext) {
			this.call = call;
			this.preemptionContext = preemptionContext;
		}

		@Override
		public LuaState getState() {
			return call.state;
		}

		@Override
		public ObjectSink getObjectSink() {
			return call.objectSink;
		}

		@Override
		public Coroutine getCurrentCoroutine() {
			return call.currentCoroutine;
		}

		@Override
		public Coroutine newCoroutine(Function function) {
			return new Coroutine(function);
		}

		@Override
		public boolean canYield() {
			return call.currentCoroutine.canYield();
		}

		@Override
		public void resume(Coroutine coroutine, Object[] args) throws ControlThrowable {
			throw new CoroutineSwitch.Resume(coroutine, args);
		}

		@Override
		public void yield(Object[] args) throws ControlThrowable {
			throw new CoroutineSwitch.Yield(args);
		}

		@Override
		public void checkPreempt(int cost) throws ControlThrowable {
			if (preemptionContext.withdraw(cost)) {
				throw new Preempted();
			}
		}

		@Override
		public void resumeAfter(Runnable task) throws ControlThrowable {
			throw new WaitForAsync(task);
		}

	}

	public interface EventHandler {

		// when true is returned, the events are ignored by the executor
		// (i.e. an implicit resume happens immediately after)

		void paused(Call c, Continuation cont, PreemptionContext preemptionContext);

		void waiting(Call c, Runnable task, Continuation cont, PreemptionContext preemptionContext);

		void returned(Call c, Object[] result);

		void failed(Call c, Throwable error);

	}

	public static class DefaultEventHandler implements EventHandler {

		private static final DefaultEventHandler INSTANCE = new DefaultEventHandler();

		@Override
		public void paused(Call c, Continuation cont, PreemptionContext preemptionContext) {
			// no-op
			// FIXME
		}

		@Override
		public void waiting(Call c, Runnable task, Continuation cont, PreemptionContext preemptionContext) {
			// FIXME
			task.run();
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

	public static class Continuation {

		private final Call call;
		private final int version;

		private Continuation(Call call, int version) {
			this.call = call;
			this.version = version;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Continuation that = (Continuation) o;

			return this.version == that.version && this.call.equals(that.call);
		}

		@Override
		public int hashCode() {
			int result = call.hashCode();
			result = 31 * result + version;
			return result;
		}

		public Callable<Object[]> toCallable(final EventHandler handler, final PreemptionContext preemptionContext) {
			return new Callable<Object[]>() {
				@Override
				public Object[] call() throws Exception {
					Object[] r = null;
					try {
						r = call.resume(new Context(call, preemptionContext), version, handler);
					}
					catch (Exception e) {
						call.result.fail(e);
						throw e;
					}

					if (r != null) {
						call.result.complete(r);
					}

					return r;
				}
			};
		}

	}

	private Continuation continuation(int version) {
		return isPaused(version) ? new Continuation(this, version) : null;
	}

	private Continuation currentContinuationOrNull() {
		return continuation(currentVersion.get());
	}

	public Continuation currentContinuation() {
		int version = currentVersion.get();

		if (!isPaused(version)) {
			State s = versionToState(version);
			throw new IllegalStateException("Cannot get continuation of a " + s + " call");
		}
		else {
			return continuation(version);
		}
	}

	private Object[] resumeCurrentContinuation(EventHandler handler, PreemptionContext preemptionContext) throws Exception {
		return currentContinuation().toCallable(handler, preemptionContext).call();
	}

	@Deprecated
	public void resume(EventHandler handler, PreemptionContext preemptionContext) {
		try {
			resumeCurrentContinuation(handler, preemptionContext);
		}
		catch (Exception e) {
			// no-op
		}
	}

	private Object[] resume(Context context, int version, final EventHandler handler) {
		if (version == VERSION_RUNNING || version == VERSION_TERMINATED || version == VERSION_CANCELLED) {
			throw new IllegalArgumentException("Illegal version: " + version);
		}

		if (!currentVersion.compareAndSet(version, VERSION_RUNNING)) {
			throw new IllegalStateException("Cannot resume call: not in the expected state");
		}

		int newVersion = VERSION_TERMINATED;
		Hook hook = null;
		try {
			hook = doResume(context, handler);
			if (hook.result == null) {
				newVersion = newPausedVersion(version);
			}
			return hook.result;
		}
		catch (final RuntimeException ex) {
			hook = new Hook(null) {
				@Override
				public Runnable body(Continuation cont) {
					return new Runnable() {
						@Override
						public void run() {
							handler.failed(Call.this, ex);
						}
					};
				}
			};
			throw ex;
		}
		finally {
			int old = currentVersion.getAndSet(newVersion);
			assert (old == VERSION_RUNNING);

			if (hook != null) {
				Runnable hookBody = hook.body(currentContinuationOrNull());
				hookBody.run();
			}
		}
	}

	private abstract static class Hook {
		public final Object[] result;

		private Hook(Object[] result) {
			this.result = result;
		}

		public abstract Runnable body(Continuation cont);

	}

	// returns null iff the execution is paused (i.e. when it can be resumed)
	private Hook doResume(final Context context, final EventHandler handler) throws RuntimeException {
		Throwable error = null;

		while (currentCoroutine != null) {
			ResumeResult result = currentCoroutine.resume(context, error);

			if (result instanceof ResumeResult.Pause) {
				return new Hook(null) {
					@Override
					public Runnable body(final Continuation cont) {
						return new Runnable() {
							@Override
							public void run() {
								handler.paused(Call.this, cont, context.preemptionContext);
							}
						};
					}
				};
			}
			if (result instanceof ResumeResult.WaitForAsync) {
				ResumeResult.WaitForAsync wait = (ResumeResult.WaitForAsync) result;
				final Runnable task = wait.task;
				return new Hook(null) {
					@Override
					public Runnable body(final Continuation cont) {
						return new Runnable() {
							@Override
							public void run() {
								handler.waiting(Call.this, task, cont, context.preemptionContext);
							}
						};
					}
				};
			}
			else if (result instanceof ResumeResult.Switch) {
				currentCoroutine = ((ResumeResult.Switch) result).target;
				error = null;
			}
			else if (result instanceof ResumeResult.ImplicitYield) {
				ResumeResult.ImplicitYield r = (ResumeResult.ImplicitYield) result;
				currentCoroutine = r.target;
				error = r.error;
			}
			else if (result instanceof ResumeResult.Error) {
				currentCoroutine = null;
				error = ((ResumeResult.Error) result).error;
			}
			else if (result instanceof ResumeResult.Finished) {
				currentCoroutine = null;
				error = null;
			}
			else {
				throw new IllegalStateException("Illegal result: " + result);
			}
		}

		if (error == null) {
			// main coroutine returned
			final Object[] result = objectSink.toArray();
			return new Hook(result) {
				@Override
				public Runnable body(Continuation cont) {
					return new Runnable() {
						@Override
						public void run() {
							handler.returned(Call.this, result);
						}
					};
				}
			};
		}
		else {
			// exception in the main coroutine
//			handler.failed(error);

			// FIXME
			throw error instanceof RuntimeException ? (RuntimeException) error : new RuntimeException(error);
		}
	}


}
