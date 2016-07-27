package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Call {

	private final LuaState state;
	private final EventHandler handler;
	private final ObjectSink objectSink;
	private final PreemptionContext preemptionContext;
	private final Context context;

	private Coroutine currentCoroutine;

	private final Random versionSource;
	private final int startingVersion;
	private final AtomicInteger currentVersion;

	private static final int VERSION_RUNNING = 0;
	private static final int VERSION_TERMINATED = 1;
	private static final int VERSION_CANCELLED = 2;

	private Call(
			LuaState state,
			PreemptionContext preemptionContext,
			EventHandler handler,
			ObjectSink objectSink,
			Coroutine mainCoroutine) {

		this.state = Check.notNull(state);
		this.preemptionContext = Check.notNull(preemptionContext);
		this.handler = Check.notNull(handler);
		this.objectSink = Check.notNull(objectSink);
		this.context = new Context();

		this.currentCoroutine = Check.notNull(mainCoroutine);

		this.versionSource = new Random();
		this.startingVersion = newPausedVersion(0);
		this.currentVersion = new AtomicInteger(startingVersion);
	}

	public static Call init(
			LuaState state,
			PreemptionContext preemptionContext,
			EventHandler handler,
			Object fn,
			Object... args) {

		ObjectSink objectSink = state.newObjectSink();
		Coroutine c = new Coroutine(fn);
		objectSink.setToArray(args);
		return new Call(state, preemptionContext, handler, objectSink, c);
	}

	public enum State {
		PAUSED,
		RUNNING,
		TERMINATED,
		CANCELLED
	}

	private int newPausedVersion(int oldVersion) {
		int v;
		do {
			v = versionSource.nextInt();
		} while (v == VERSION_RUNNING
				|| v == VERSION_TERMINATED
				|| v == VERSION_CANCELLED
				|| v == oldVersion);
		return v;
	}


	public State state() {
		switch (currentVersion.get()) {
			case VERSION_RUNNING:    return State.RUNNING;
			case VERSION_TERMINATED: return State.TERMINATED;
			case VERSION_CANCELLED:  return State.CANCELLED;
			default:                 return State.PAUSED;
		}
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

	protected class Context implements ExecutionContext {

		@Override
		public LuaState getState() {
			return state;
		}

		@Override
		public ObjectSink getObjectSink() {
			return objectSink;
		}

		@Override
		public Coroutine getCurrentCoroutine() {
			return currentCoroutine;
		}

		@Override
		public Coroutine newCoroutine(Function function) {
			return new Coroutine(function);
		}

		@Override
		public boolean canYield() {
			return currentCoroutine.canYield();
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
			preemptionContext.withdraw(cost);
		}

		@Override
		public void resumeAfter(Runnable task) throws ControlThrowable {
			throw new WaitForAsync(task);
		}

	}

	public interface EventHandler {

		// when true is returned, the events are ignored by the executor
		// (i.e. an implicit resume happens immediately after)

		void paused(Call c);

		void waiting(Call c, Runnable task);

		void returned(Call c, Object[] result);

		void failed(Call c, Throwable error);

	}

	public static class DefaultEventHandler implements EventHandler {

		private static final DefaultEventHandler INSTANCE = new DefaultEventHandler();

		@Override
		public void paused(Call c) {
			// no-op
			// FIXME
		}

		@Override
		public void waiting(Call c, Runnable task) {
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

	public static class Continuation implements Callable<Object[]> {

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

		@Override
		public Object[] call() throws Exception {
			Object[] r = null;
			try {
				r = call.resume(version);
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

	}

	public Callable<Object[]> currentContinuationCallable() {
		int version = currentVersion.get();
		switch (version) {
			case VERSION_RUNNING:    throw new IllegalStateException("Cannot get continuation of a running call");
			case VERSION_TERMINATED: throw new IllegalStateException("Cannot get continuation of a terminated call");
			case VERSION_CANCELLED:  throw new IllegalStateException("Cannot get continuation of a cancelled call");
		}

		return new Continuation(this, version);
	}

	@Deprecated
	public FutureTask<Object[]> currentContinuationTask() {
		return new FutureTask<>(currentContinuationCallable());
	}

	private Object[] resumeCurrentContinuation() throws Exception {
		return currentContinuationCallable().call();
	}

	@Deprecated
	public void resume() {
		try {
			resumeCurrentContinuation();
		}
		catch (Exception e) {
			// no-op
		}
	}

	private Object[] resume(int version) {
		if (version == VERSION_RUNNING || version == VERSION_TERMINATED || version == VERSION_CANCELLED) {
			throw new IllegalArgumentException("Illegal version: " + version);
		}

		if (!currentVersion.compareAndSet(version, VERSION_RUNNING)) {
			throw new IllegalStateException("Cannot resume call: not in the expected state");
		}

		int newVersion = VERSION_TERMINATED;
		Hook hook = null;
		try {
			hook = doResume();
			if (hook.result == null) {
				newVersion = newPausedVersion(version);
			}
			return hook.result;
		}
		catch (final RuntimeException ex) {
			hook = new Hook(null, new Runnable() {
				@Override
				public void run() {
					handler.failed(Call.this, ex);
				}
			});
			throw ex;
		}
		finally {
			int old = currentVersion.getAndSet(newVersion);
			assert (old == VERSION_RUNNING);

			if (hook != null && hook.body != null) {
				hook.body.run();
			}
		}
	}

	private static class Hook {
		public final Object[] result;
		public final Runnable body;
		private Hook(Object[] result, Runnable body) {
			this.result = result;
			this.body = body;
		}
	}

	// returns null iff the execution is paused (i.e. when it can be resumed)
	private Hook doResume() throws RuntimeException {
		Throwable error = null;

		while (currentCoroutine != null) {
			ResumeResult result = currentCoroutine.resume(context, error);

			if (result instanceof ResumeResult.Pause) {
				return new Hook(null, new Runnable() {
					@Override
					public void run() {
						handler.paused(Call.this);
					}
				});
			}
			if (result instanceof ResumeResult.WaitForAsync) {
				ResumeResult.WaitForAsync wait = (ResumeResult.WaitForAsync) result;
				final Runnable task = wait.task;
				return new Hook(null, new Runnable() {
					@Override
					public void run() {
						handler.waiting(Call.this, task);
					}
				});
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
			return new Hook(result, new Runnable() {
				@Override
				public void run() {
					handler.returned(Call.this, result);
				}
			});
		}
		else {
			// exception in the main coroutine
//			handler.failed(error);

			// FIXME
			throw error instanceof RuntimeException ? (RuntimeException) error : new RuntimeException(error);
		}
	}


}
