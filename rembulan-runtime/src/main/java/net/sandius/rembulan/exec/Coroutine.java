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

import net.sandius.rembulan.Dispatch;
import net.sandius.rembulan.ExecutionContext;
import net.sandius.rembulan.Resumable;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

public final class Coroutine {

	// paused call stack: up-to-date only iff coroutine is not running
	private Cons<ResumeInfo> callStack;
	private Status status;

	public Coroutine(Object function) {
		this.callStack = new Cons<>(new ResumeInfo(BootstrapResumable.INSTANCE, Check.notNull(function)));
		this.status = Status.SUSPENDED;
	}

	private enum Status {
		SUSPENDED,
		RUNNING,
		NORMAL,
		DEAD
	}

	private synchronized Status getStatus() {
		return status;
	}

	public boolean isResuming() {
		return getStatus() == Status.NORMAL;
	}

	public boolean isDead() {
		return getStatus() == Status.DEAD;
	}

	@Override
	public String toString() {
		return "thread: 0x" + Integer.toHexString(hashCode());
	}

	protected static class BootstrapResumable implements Resumable {

		static final BootstrapResumable INSTANCE = new BootstrapResumable();

		@Override
		public void resume(ExecutionContext context, Object target) throws ControlThrowable {
			Dispatch.call(context, target, context.getReturnBuffer().getAsArray());
		}

	}

	// (RUNNING, SUSPENDED) -> (NORMAL, RUNNING)
	static Cons<ResumeInfo> _resume(final Coroutine a, final Coroutine b, Cons<ResumeInfo> cs) {
		Check.notNull(a);
		Check.notNull(b);
		Check.notNull(cs);

		synchronized (a) {
			if (a.status == Status.RUNNING) {
				synchronized (b) {
					if (b.status == Status.SUSPENDED) {
						Cons<ResumeInfo> result = b.callStack;
						a.callStack = cs;
						b.callStack = null;
						a.status = Status.NORMAL;
						b.status = Status.RUNNING;
						return result;
					}
					else {
						if (b.status == Status.DEAD) {
							throw new IllegalCoroutineStateException("cannot resume dead coroutine");
						}
						else {
							throw new IllegalCoroutineStateException("cannot resume non-suspended coroutine");
						}
					}
				}
			}
			else {
				throw new IllegalStateException("resuming coroutine not in running state");
			}
		}
	}

	// (NORMAL, RUNNING) -> (RUNNING, SUSPENDED)
	static Cons<ResumeInfo> _yield(final Coroutine a, final Coroutine b, Cons<ResumeInfo> cs) {
		synchronized (a) {
			if (a.status == Status.NORMAL) {
				synchronized (b) {
					if (b.status == Status.RUNNING) {
						Cons<ResumeInfo> result = a.callStack;
						a.callStack = null;
						b.callStack = cs;
						a.status = Status.RUNNING;
						b.status = b.callStack != null ? Status.SUSPENDED : Status.DEAD;
						return result;
					}
					else {
						throw new IllegalCoroutineStateException("yielding coroutine not in running state");
					}
				}
			}
			else {
				throw new IllegalCoroutineStateException("yielding coroutine not in normal state");
			}
		}
	}

	// (NORMAL, RUNNING) -> (RUNNING, DEAD)
	static Cons<ResumeInfo> _return(Coroutine a, Coroutine b) {
		return _yield(a, b, null);
	}

	synchronized Cons<ResumeInfo> unpause() {
		// TODO: check status?
		status = Status.RUNNING;
		Cons<ResumeInfo> result = callStack;
		callStack = null;
		return result;
	}

	synchronized void pause(Cons<ResumeInfo> callStack) {
		// TODO: check status?
		this.callStack = callStack;
	}

}
