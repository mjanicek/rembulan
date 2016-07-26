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
	private final ObjectSink objectSink;

	private final Context context;

	private Coroutine currentCoroutine;

	private final Random versionSource;
	private final AtomicInteger currentVersion;

	private static final int VERSION_RUNNING = 0;
	private static final int VERSION_TERMINATED = 1;

	private Call(LuaState state, ObjectSink objectSink, Coroutine mainCoroutine) {
		this.state = Check.notNull(state);
		this.objectSink = Check.notNull(objectSink);
		this.currentCoroutine = Check.notNull(mainCoroutine);

		this.context = new Context();

		this.versionSource = new Random();
		this.currentVersion = new AtomicInteger(newVersion(0));
	}

	public static Call init(LuaState state, Object fn, Object... args) {
		ObjectSink objectSink = state.newObjectSink();
		Coroutine c = new Coroutine(fn);
		objectSink.setToArray(args);
		return new Call(state, objectSink, c);
	}

	public enum State {
		PAUSED,
		RUNNING,
		TERMINATED
	}

	private int newVersion(int oldVersion) {
		int v;
		do {
			v = versionSource.nextInt();
		} while (v == VERSION_RUNNING || v == VERSION_TERMINATED || v == oldVersion);
		return v;
	}


	public State state() {
		switch (currentVersion.get()) {
			case VERSION_RUNNING:    return State.RUNNING;
			case VERSION_TERMINATED: return State.TERMINATED;
			default:                 return State.PAUSED;
		}
	}

	private static class ResultFuture implements Future<Object[]> {

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
				if (mayInterruptIfRunning) {
					// TODO: do interrupt
				}
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

	@Deprecated
	public ExecutionState getExecutionState() {
		switch (state()) {
			case PAUSED:     return ExecutionState.PAUSED;
			case RUNNING:    return ExecutionState.RUNNING;
			default:  throw new IllegalStateException();  // FIXME
		}
	}

	@Deprecated
	public LuaState getState() {
		return state;
	}

	@Deprecated
	public ObjectSink getSink() {
		return objectSink;
	}

	@Deprecated
	public boolean isPaused() {
		return currentCoroutine != null && currentCoroutine.isPaused();
	}

	@Deprecated
	protected Coroutine getCurrentCoroutine() {
		return currentCoroutine;
	}

	@Deprecated
	public ExecutionContext getContext() {
		return context;
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

	}

	public interface EventHandler {

		// when true is returned, the events are ignored by the executor
		// (i.e. an implicit resume happens immediately after)

		boolean paused();

	}

	public static class DefaultEventHandler implements EventHandler {

		private static final DefaultEventHandler INSTANCE = new DefaultEventHandler();

		@Override
		public boolean paused() {
			return false;
		}

	}

	private class ContinuationCallable implements Callable<Object[]> {

		private final EventHandler handler;
		private final int version;

		ContinuationCallable(EventHandler handler, int version) {
			this.handler = Check.notNull(handler);
			this.version = version;
		}

		@Override
		public Object[] call() throws Exception {
			return resume(handler, version);
		}

	}

	public Callable<Object[]> continuationCallable(EventHandler handler) {
		int version = currentVersion.get();
		switch (version) {
			case VERSION_RUNNING:    throw new IllegalStateException("Cannot get continuation of a running call");
			case VERSION_TERMINATED: throw new IllegalStateException("Cannot get continuation of a terminated call");
		}

		return new ContinuationCallable(handler, version);
	}

	public FutureTask<Object[]> continuationTask(EventHandler handler) {
		return new FutureTask<>(continuationCallable(handler));
	}

	private Object[] resume(EventHandler handler) throws Exception {
		return continuationCallable(handler).call();
	}

	@Deprecated
	public ExecutionState resume() {
		Object[] r;
		try {
			r = resume(DefaultEventHandler.INSTANCE);
		}
		catch (Exception e) {
			result.fail(e);
			return new ExecutionState.TerminatedAbnormally(e);
		}

		if (r == null) {
			return ExecutionState.PAUSED;
		}
		else {
			result.complete(r);
			return ExecutionState.TERMINATED_NORMALLY;
		}
	}

	private Object[] resume(EventHandler handler, int version) {
		Check.notNull(handler);

		if (version == VERSION_RUNNING || version == VERSION_TERMINATED) {
			throw new IllegalArgumentException("Illegal version: " + version);
		}

		if (!currentVersion.compareAndSet(version, VERSION_RUNNING)) {
			throw new IllegalStateException("Cannot resume a coroutine the current continuation");
		}

		int newVersion = VERSION_TERMINATED;
		try {
			Object[] result = doResume(handler);
			if (result == null) {
				newVersion = newVersion(version);
			}
			return result;
		}
		finally {
			int old = currentVersion.getAndSet(newVersion);
			assert (old == VERSION_RUNNING);
		}
	}

	// returns null iff the execution is paused (i.e. when it can be resumed)
	private Object[] doResume(EventHandler handler) throws RuntimeException {
		Throwable error = null;

		while (currentCoroutine != null) {
			ResumeResult result = currentCoroutine.resume(context, error);

			if (result instanceof ResumeResult.Pause) {
				if (!handler.paused()) {
					return null;
				}
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
			return objectSink.toArray();
		}
		else {
			// exception in the main coroutine
			// FIXME
			throw error instanceof RuntimeException ? (RuntimeException) error : new RuntimeException(error);
		}
	}


}
