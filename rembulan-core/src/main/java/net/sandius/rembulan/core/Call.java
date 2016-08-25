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

import net.sandius.rembulan.core.exec.AsyncTask;
import net.sandius.rembulan.util.Check;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Call {

	private final LuaState state;
	private final ReturnBuffer returnBuffer;

	private Coroutine currentCoroutine;

	private final AtomicInteger currentVersion;

	private static final int VERSION_RUNNING = 0;
	private static final int VERSION_TERMINATED = 1;

	private Call(
			LuaState state,
			ReturnBuffer returnBuffer,
			Coroutine mainCoroutine) {

		this.state = Check.notNull(state);
		this.returnBuffer = Check.notNull(returnBuffer);

		this.currentCoroutine = Check.notNull(mainCoroutine);

		int startingVersion = newPausedVersion(0);
		this.currentVersion = new AtomicInteger(startingVersion);
	}

	public static Call init(
			LuaState state,
			Object fn,
			Object... args) {

		ReturnBuffer returnBuffer = state.newReturnBuffer();
		Coroutine c = new Coroutine(fn);
		returnBuffer.setToContentsOf(args);
		return new Call(state, returnBuffer, c);
	}

	public enum State {
		PAUSED,
		RUNNING,
		TERMINATED
	}

	private static int newPausedVersion(int oldVersion) {
		int v;
		Random versionSource = ThreadLocalRandom.current();
		do {
			v = versionSource.nextInt();
		} while (!isPaused(v) || v == oldVersion);
		return v;
	}

	private static boolean isPaused(int version) {
		return version != VERSION_RUNNING
				&& version != VERSION_TERMINATED;
	}

	private static State versionToState(int version) {
		switch (version) {
			case VERSION_RUNNING:    return State.RUNNING;
			case VERSION_TERMINATED: return State.TERMINATED;
			default:                 return State.PAUSED;
		}
	}

	public State state() {
		return versionToState(currentVersion.get());
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
		public ReturnBuffer getReturnBuffer() {
			return call.returnBuffer;
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
		public void resumeAfter(AsyncTask task) throws ControlThrowable {
			throw new WaitForAsync(task);
		}

	}

	public interface EventHandler {

		void returned(Call c, Object[] result);

		void failed(Call c, Throwable error);

		void paused(Call c, Continuation cont);

		void async(Call c, Continuation cont, AsyncTask task);

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

		public Runnable toRunnable(final EventHandler handler, final PreemptionContext preemptionContext) {
			return new Runnable() {
				@Override
				public void run() {
					resume(handler, preemptionContext);
				}
			};
		}

		public void resume(EventHandler handler, PreemptionContext preemptionContext) {
			call.resume(new Context(call, preemptionContext), version, handler);
		}

	}

	private Continuation continuation(int version) {
		return isPaused(version) ? new Continuation(this, version) : null;
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

	private void resume(Context context, int version, EventHandler handler) {
		if (version == VERSION_RUNNING || version == VERSION_TERMINATED) {
			throw new IllegalArgumentException("Illegal version: " + version);
		}

		// claim this version
		if (!currentVersion.compareAndSet(version, VERSION_RUNNING)) {
			throw new OutdatedContinuationException("Cannot resume call: not in the expected state (0x"
					+ Integer.toHexString(version) + ")");
		}

		int newVersion = VERSION_TERMINATED;
		RResult rr = null;
		Continuation cont = null;
		try {
			rr = doResume(context);
			assert (rr != null);
			if (rr.pause) {
				newVersion = newPausedVersion(version);
				cont = new Continuation(this, newVersion);
			}
		}
		finally {
			int old = currentVersion.getAndSet(newVersion);
			assert (old == VERSION_RUNNING);
		}

		assert (rr != null);

		rr.fire(handler, this, cont);
	}

	private static final class RResult {
		private final boolean pause;
		private final Object[] values;
		private final Throwable error;
		private final AsyncTask asyncTask;

		private RResult(boolean pause, Object[] values, Throwable error, AsyncTask asyncTask) {
			// validate: at most one may be true/non-null
			if ((pause && (values == null && error == null && asyncTask == null))
					|| (values != null && (!pause && error == null && asyncTask == null))
					|| (error != null && (!pause && values == null && asyncTask == null))
					|| (asyncTask != null && (!pause && values == null && asyncTask == null))) {
				this.pause = pause;
				this.values = values;
				this.error = error;
				this.asyncTask = asyncTask;
			}
			else {
				throw new IllegalArgumentException("Illegal arguments: "
						+ pause + ", " + Arrays.toString(values) + ", " + error);
			}
		}

		void fire(EventHandler handler, Call c, Continuation cont) {
			if (pause) {
				handler.paused(c, Check.notNull(cont));
			}
			else if (values != null) {
				handler.returned(c, values);
			}
			else if (error != null) {
				handler.failed(c, error);
			}
			else if (asyncTask != null) {
				handler.async(c, Check.notNull(cont), asyncTask);
			}
			else {
				throw new AssertionError();
			}
		}
	}

	private static final RResult PAUSE_RESULT = new RResult(true, null, null, null);

	private RResult doResume(ExecutionContext context) {
		Throwable error = null;

		while (currentCoroutine != null) {
			ResumeResult result = currentCoroutine.resume(context, error);

			if (result instanceof ResumeResult.Pause) {
				return PAUSE_RESULT;
			}
			if (result instanceof ResumeResult.WaitForAsync) {
				ResumeResult.WaitForAsync wait = (ResumeResult.WaitForAsync) result;
				return new RResult(false, null, null, wait.task);
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
			final Object[] result = returnBuffer.getAsArray();
			assert (result != null);
			return new RResult(false, result, null, null);
		}
		else {
			// exception in the main coroutine
			return new RResult(false, null, error, null);
		}
	}


}
