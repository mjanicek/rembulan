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

/**
 * A throwable used for non-local control flow changes, containing an incomplete
 * (<i>unresolved</i>) Lua call stack without the bottom-most frame.
 *
 * <p>When suspending a call, the runtime uses the exception handling mechanism of the
 * Java Virtual Machine to unravel the (Java) call stack and while doing so, to build
 * a representation of the Lua call stack for a later resume. This throwable is an essential
 * part of this mechanism.</p>
 *
 * <p>An {@code UnresolvedControlThrowable} contains an incomplete call stack, missing the
 * bottom-most call frame. In order to <i>resolve</i> the control throwable, a call frame must
 * be attached by calling the method {@link #resolve(Resumable, Object)}, and the resulting
 * {@code ResolvedControlThrowable} must be thrown. When this process is repeated on every
 * level of the Lua call stack all the way down to the entry point, a full Lua call stack
 * representation will be built.</p>
 *
 * <p>Consequently, <b>this throwable should never be ignored</b>. Doing so would prevent
 * both the non-local control change (i.e., the suspend) and the construction of the Lua call
 * stack.</p>
 *
 * <p>Instances of this class are immutable and do not contain Java stack traces for performance
 * reasons.</p>
 */
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

	/**
	 * Resolves this control throwable by prepending a call frame to it. The resulting
	 * {@link ResolvedControlThrowable} <b>must be thrown</b> in order to continue
	 * unravelling the call stack.
	 *
	 * <p>When resuming this call, the runtime will use {@code resumable} as the function
	 * object used to continue the execution, and supply it with the {@code suspendedState}
	 * object (or an object <i>equivalent</i> to it, see {@link Resumable}) registered
	 * by this method.</p>
	 *
	 * @param resumable  the resumable at this level, must not be {@code null}
	 * @param suspendedState  the suspended state, may be any value
	 * @return  a resolved control throwable, <b>must be thrown</b>
	 *
	 * @throws NullPointerException  if {@code resumable} is {@code null}
	 */
	public ResolvedControlThrowable resolve(Resumable resumable, Object suspendedState) {
		return new ResolvedControlThrowable(payload,
				new Cons<>(new ResumeInfo(resumable, suspendedState), resumeStack));
	}

	ResolvedControlThrowable resolve() {
		return new ResolvedControlThrowable(payload, resumeStack);
	}

}
