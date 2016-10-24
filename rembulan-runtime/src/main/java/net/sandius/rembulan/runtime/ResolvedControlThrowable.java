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

import net.sandius.rembulan.util.Cons;

import java.util.Iterator;
import java.util.Objects;

/**
 * A throwable used for non-local control flow changes, containing a complete
 * (<i>resolved</i>) Lua call stack information all the way to the stack top.
 *
 * <p>When suspending a call, the runtime uses the exception handling mechanism of the
 * Java Virtual Machine to unravel the (Java) call stack and while doing so, to build
 * a representation of the Lua call stack for a later resume. This throwable is an essential
 * part of this mechanism.</p>
 *
 * <p>In contrast to {@link UnresolvedControlThrowable}, a {@code ResolvedControlThrowable}
 * contains a complete representation of the Lua call stack that can be used to resume
 * the call. In order to unravel the entire Lua call stack up to the entry
 * point of the call, <b>this throwable should never be caught without being rethrown</b>.
 * Doing so would prevent both the non-local control change (i.e., the suspend) and the
 * construction of the remainder of the Lua call stack.</p>
 *
 * <p>Instances of this class are immutable and do not contain Java stack traces for performance
 * reasons.</p>
 */
public final class ResolvedControlThrowable extends Throwable {

	private final ControlThrowablePayload payload;
	private final Cons<ResumeInfo> resumeStack;

	ResolvedControlThrowable(ControlThrowablePayload payload, Cons<ResumeInfo> resumeStack) {
		super(null, null, true, false);
		this.payload = Objects.requireNonNull(payload);
		this.resumeStack = resumeStack;
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
