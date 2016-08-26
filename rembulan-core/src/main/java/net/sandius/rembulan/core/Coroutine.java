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
import net.sandius.rembulan.util.Cons;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Coroutine {

	private final Lock lock;

	// paused call stack: up-to-date only iff coroutine is not running
	private Cons<ResumeInfo> callStack;
	private Status status;

	public Coroutine(Object function) {
		this.lock = new ReentrantLock();

		this.callStack = new Cons<>(new ResumeInfo(BootstrapResumable.INSTANCE, Check.notNull(function)));
		this.status = Status.SUSPENDED;
	}

	private enum Status {
		SUSPENDED,
		RUNNING,
		NORMAL,
		DEAD
	}

	private Status getStatus() {
		lock.lock();
		try {
			return status;
		}
		finally {
			lock.unlock();
		}
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
	static Cons<ResumeInfo> _resume(Coroutine a, Coroutine b, Cons<ResumeInfo> cs) {
		Check.notNull(cs);

		a.lock.lock();
		try {
			if (a.status == Status.RUNNING) {
				try {
					b.lock.lock();
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
				finally {
					b.lock.unlock();
				}
			}
			else {
				throw new IllegalStateException("resuming coroutine not in running state");
			}
		}
		finally {
			a.lock.unlock();
		}
	}

	// (NORMAL, RUNNING) -> (RUNNING, SUSPENDED)
	static Cons<ResumeInfo> _yield(Coroutine a, Coroutine b, Cons<ResumeInfo> cs) {
		a.lock.lock();
		try {
			if (a.status == Status.NORMAL) {
				b.lock.lock();
				try {
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
				finally {
					b.lock.unlock();
				}
			}
			else {
				throw new IllegalCoroutineStateException("yielding coroutine not in normal state");
			}
		}
		finally {
			a.lock.unlock();
		}
	}

	// (NORMAL, RUNNING) -> (RUNNING, DEAD)
	static Cons<ResumeInfo> _return(Coroutine a, Coroutine b) {
		return _yield(a, b, null);
	}

	Cons<ResumeInfo> unpause() {
		// TODO: check status?
		lock.lock();
		try {
			status = Status.RUNNING;
			Cons<ResumeInfo> result = callStack;
			callStack = null;
			return result;
		}
		finally {
			lock.unlock();
		}
	}

	void pause(Cons<ResumeInfo> callStack) {
		// TODO: check status?
		lock.lock();
		try {
			this.callStack = callStack;
		}
		finally {
			lock.unlock();
		}
	}

}
