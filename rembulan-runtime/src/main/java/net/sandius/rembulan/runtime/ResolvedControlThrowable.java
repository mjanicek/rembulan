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

package net.sandius.rembulan.runtime;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

import java.util.Iterator;

public final class ResolvedControlThrowable extends Throwable {

	private final ControlThrowablePayload payload;
	private final Cons<ResumeInfo> resumeStack;

	ResolvedControlThrowable(ControlThrowablePayload payload, Cons<ResumeInfo> resumeStack) {
		super(null, null, true, false);
		this.payload = Check.notNull(payload);
		this.resumeStack = resumeStack;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return null;
	}

	ControlThrowablePayload payload() {
		return payload;
	}

	// LIFO iterator
	Iterator<ResumeInfo> frames() {
		return Cons.newIterator(resumeStack);
	}

	UnresolvedControlThrowable unresolve() {
		return new UnresolvedControlThrowable(payload, resumeStack);
	}

}
