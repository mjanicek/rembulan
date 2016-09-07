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

package net.sandius.rembulan;

import net.sandius.rembulan.exec.ResolvedControlThrowable;
import net.sandius.rembulan.exec.UnresolvedControlThrowable;

/**
 * An interface for resuming suspended protected Lua function calls.
 */
public interface ProtectedResumable extends Resumable {

	/**
	 * Resumes this protected resumable in the given execution context {@code context},
	 * passing the suspended state {@code suspendedState} and an error object {@code error}
	 * to it.
	 *
	 * <p>This method is called by the call executor when resuming a previously suspended
	 * protected function call while unrolling the call stack after an error has occurred.
	 * The error, passed in the {@code error} argument, may be {@code null}.</p>
	 *
	 * <p>As with {@link Resumable#resume(ExecutionContext, Object)}, the execution context
	 * {@code context} may differ from the execution context used before, and {@code suspendedState}
	 * is guaranteed by the executor to be <i>equivalent</i> to the suspended state registered
	 * by the call to {@link UnresolvedControlThrowable#push(Resumable, Object)}. Equivalence here
	 * means that the class will be equal to that of the state object previously registered;
	 * however, the actual instance passed to this method may be a clone or a (serialised
	 * and) de-serialised version of the original object.</p>
	 *
	 * <p>Implementations of this method may safely assume that {@code context} is not
	 * {@code null}.</p>
	 *
	 * @param context  execution context, non-{@code null} when called by a call executor
	 * @param suspendedState  suspended state, equivalent to the suspended state registered
	 *                        with the call executor
	 * @param error  error object, may be {@code null}
	 *
	 * @throws ResolvedControlThrowable  if the resumed call initiates a non-local control change
	 */
	void resumeError(ExecutionContext context, Object suspendedState, Object error)
			throws ResolvedControlThrowable;

}
