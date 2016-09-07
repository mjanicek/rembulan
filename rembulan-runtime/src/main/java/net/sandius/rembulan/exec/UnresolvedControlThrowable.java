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

import net.sandius.rembulan.Resumable;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

public final class UnresolvedControlThrowable extends Throwable {

	private final ControlThrowablePayload payload;
	private final Cons<ResumeInfo> resumeStack;

	UnresolvedControlThrowable(ControlThrowablePayload payload, Cons<ResumeInfo> resumeStack) {
		super(null, null, true, false);
		this.payload = Check.notNull(payload);
		this.resumeStack = resumeStack;
	}

	UnresolvedControlThrowable(ControlThrowablePayload payload) {
		this(payload, null);
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return null;
	}

	public ResolvedControlThrowable push(Resumable resumable, Object suspendedState) {
		return new ResolvedControlThrowable(payload,
				new Cons<>(new ResumeInfo(resumable, suspendedState), resumeStack));
	}

	public ResolvedControlThrowable ignoreFrame() {
		return new ResolvedControlThrowable(payload, resumeStack);
	}

}
