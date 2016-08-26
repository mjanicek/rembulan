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
import net.sandius.rembulan.util.Cons;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Call {

	private final LuaState state;
	private final ReturnBuffer returnBuffer;

//	private Coroutine currentCoroutine;
	private Cons<Coroutine> coroutineStack;

	private final AtomicInteger currentVersion;

	private static final int VERSION_RUNNING = 0;
	private static final int VERSION_TERMINATED = 1;

	private Call(
			LuaState state,
			ReturnBuffer returnBuffer,
			Coroutine mainCoroutine) {

		this.state = Check.notNull(state);
		this.returnBuffer = Check.notNull(returnBuffer);

//		this.currentCoroutine = Check.notNull(mainCoroutine);
		this.coroutineStack = new Cons<>(Check.notNull(mainCoroutine));

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

	private class Context implements ExecutionContext {

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
			return coroutineStack.car;
		}

		@Override
		public Coroutine newCoroutine(Function function) {
			return new Coroutine(function);
		}

		@Override
		public boolean canYield() {
			return coroutineStack.cdr != null;
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

	private class CallContinuation implements Continuation {

		private final int version;

		private CallContinuation(int version) {
			this.version = version;
		}

		private Call outer() {
			return Call.this;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			CallContinuation that = (CallContinuation) o;

			return this.version == that.version && this.outer().equals(that.outer());
		}

		@Override
		public int hashCode() {
			int result = outer().hashCode();
			result = 31 * result + version;
			return result;
		}

		public Runnable toRunnable(final CallEventHandler handler, final PreemptionContext preemptionContext) {
			return new Runnable() {
				@Override
				public void run() {
					resume(handler, preemptionContext);
				}
			};
		}

		@Override
		public void resume(CallEventHandler handler, PreemptionContext preemptionContext) {
			Call.this.resume(new Context(Call.this, preemptionContext), version, handler);
		}

	}

	private Continuation continuation(int version) {
		return isPaused(version) ? new CallContinuation(version) : null;
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

	private void resume(Context context, int version, CallEventHandler handler) {
		if (version == VERSION_RUNNING || version == VERSION_TERMINATED) {
			throw new IllegalArgumentException("Illegal version: " + version);
		}

		// claim this version
		if (!currentVersion.compareAndSet(version, VERSION_RUNNING)) {
			throw new OutdatedContinuationException("Cannot resume call: not in the expected state (0x"
					+ Integer.toHexString(version) + ")");
		}

		int newVersion = VERSION_TERMINATED;
		ResumeResult rr = null;
		Continuation cont = null;
		try {
			Resumer resumer = new Resumer(context);
			rr = resumer.resume();
			assert (rr != null);
			if (rr.pause) {
				newVersion = newPausedVersion(version);
				cont = new CallContinuation(newVersion);
			}
		}
		finally {
			int old = currentVersion.getAndSet(newVersion);
			assert (old == VERSION_RUNNING);
		}

		assert (rr != null);

		rr.fire(handler, this, cont);
	}

	private static final class ResumeResult {
		private final boolean pause;
		private final Object[] values;
		private final Throwable error;
		private final AsyncTask asyncTask;

		private ResumeResult(boolean pause, Object[] values, Throwable error, AsyncTask asyncTask) {
			// validate: at most one may be true/non-null
			if ((pause && (values == null && error == null && asyncTask == null))
					|| (values != null && (!pause && error == null && asyncTask == null))
					|| (error != null && (!pause && values == null && asyncTask == null))
					|| (asyncTask != null && (!pause && values == null && error == null))) {
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

		void fire(CallEventHandler handler, Call c, Continuation cont) {
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

	private static final ResumeResult PAUSE_RESULT = new ResumeResult(true, null, null, null);

	class Resumer implements ControlThrowableVisitor {

		private final ExecutionContext context;

		private ResumeResult result;
		private Throwable error;
		private Cons<ResumeInfo> callStack;

		Resumer(ExecutionContext context) {
			this.context = context;
			this.result = null;
			this.error = null;
		}

		@Override
		public void preempted() {
			result = PAUSE_RESULT;
		}

		@Override
		public void coroutineYield(Object[] values) {
			assert (coroutineStack != null);

			if (coroutineStack.cdr == null) {
				error = new IllegalOperationAttemptException("attempt to yield from outside a coroutine");
			}
			else {
				Coroutine top = coroutineStack.car;
				Coroutine prev = coroutineStack.cdr.car;

				boolean yielded = false;
				try {
					callStack = Coroutine._yield(prev, top, callStack);
					yielded = true;
				}
				catch (IllegalCoroutineStateException ex) {
					error = ex;
				}

				if (yielded) {
					coroutineStack = coroutineStack.cdr;
					context.getReturnBuffer().setToContentsOf(values);
				}
			}
		}

		public void coroutineReturn() {
			assert (coroutineStack != null);

			if (coroutineStack.cdr == null) {
				// this was the main coroutine
				if (error == null) {
					Object[] values = context.getReturnBuffer().getAsArray();
					result = new ResumeResult(false, values, null, null);
				}
				else {
					result = new ResumeResult(false, null, error, null);
				}
			}
			else {
				// an implicit yield
				Coroutine top = coroutineStack.car;
				Coroutine prev = coroutineStack.cdr.car;

				boolean yielded = false;
				try {
					callStack = Coroutine._return(prev, top);
					yielded = true;
				}
				catch (IllegalCoroutineStateException ex) {
					error = ex;
				}

				if (yielded) {
					coroutineStack = coroutineStack.cdr;
				}
			}
		}

		@Override
		public void coroutineResume(Coroutine target, Object[] values) {
			assert (coroutineStack != null);

			Coroutine prev = coroutineStack.car;

			boolean resumed = false;
			try {
				callStack = Coroutine._resume(prev, target, callStack);
				resumed = true;
			}
			catch (IllegalCoroutineStateException ex) {
				error = ex;
			}

			if (resumed) {
				coroutineStack = new Cons<>(target, coroutineStack);
				context.getReturnBuffer().setToContentsOf(values);
			}
		}

		@Override
		public void async(AsyncTask task) {
			result = new ResumeResult(false, null, null, task);
		}

		private void saveFrames(ControlThrowable ct) {
			Iterator<ResumeInfo> it = ct.frames();
			while (it.hasNext()) {
				callStack = new Cons<>(it.next(), callStack);
			}
		}

		private void continueCurrentCoroutine() {
			while (callStack != null) {
				ResumeInfo top = callStack.car;
				callStack = callStack.cdr;

				try {
					if (error == null) {
						// no errors
						top.resume(context);
						Dispatch.evaluateTailCalls(context);
					}
					else {
						// there is an error to be handled
						if (top.resumable instanceof ProtectedResumable) {
							// top is protected, can handle the error
							Throwable e = error;
							error = null;  // this exception will be handled

							ProtectedResumable pr = (ProtectedResumable) top.resumable;
							pr.resumeError(context, top.savedState, Conversions.toErrorObject(e));
							Dispatch.evaluateTailCalls(context);
						}
						else {
							// top is not protected, continue unwinding the stack
						}
					}
				}
				catch (ControlThrowable ct) {
					saveFrames(ct);
					ct.accept(this);
					return;
				}
				catch (Exception ex) {
					// unhandled exception: will try finding a handler in the next iteration
					error = ex;
				}
			}

			assert (callStack == null);

			coroutineReturn();
		}

		ResumeResult resume() {
			try {
				callStack = coroutineStack.car.unpause();
				do {
					continueCurrentCoroutine();
				} while (result == null);

				return result;
			}
			finally {
				if (coroutineStack != null) {
					coroutineStack.car.pause(callStack);
				}
			}
		}

	}

}
