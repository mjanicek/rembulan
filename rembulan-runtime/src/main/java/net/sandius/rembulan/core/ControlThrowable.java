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

import net.sandius.rembulan.Resumable;
import net.sandius.rembulan.runtime.AsyncTask;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

import java.util.Iterator;

public final class ControlThrowable extends Throwable {

	private final Payload payload;
	private Cons<ResumeInfo> resumeStack;

	ControlThrowable(Payload payload) {
		super(null, null, true, false);
		this.payload = Check.notNull(payload);
		this.resumeStack = null;
	}

	public ControlThrowable push(Resumable resumable, Object suspendedState) {
		resumeStack = new Cons<>(new ResumeInfo(resumable, suspendedState), resumeStack);
		return this;
	}

	// LIFO iterator
	public Iterator<ResumeInfo> frames() {
		return Cons.newIterator(resumeStack);
	}

	public void accept(Visitor visitor) {
		payload.accept(visitor);
	}

	interface Payload {

		void accept(Visitor visitor);

	}

	public interface Visitor {

		void preempted();

		void coroutineYield(Object[] values);

		void coroutineResume(Coroutine target, Object[] values);

		void async(AsyncTask task);

	}

}
