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

/**
 * An interface for resuming suspended Lua function calls.
 */
public interface Resumable {

	/**
	 * Resumes this resumable in the given execution context {@code context}, passing
	 * the suspended state {@code suspendedState} to it.
	 * <b>This method throws a {@link ResolvedControlThrowable}</b>:
	 * this method is expected to have resolved non-local control changes up to the point
	 * of its invocation.
	 *
	 * <p>This method is called by the call executor when resuming a previously suspended
	 * function call, possibly with a different execution context. {@code suspendedState}
	 * is guaranteed by the executor to be <i>equivalent</i> to the suspended state registered
	 * by the call to {@link UnresolvedControlThrowable#resolve(Resumable, Object)}. Equivalence here
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
	 *
	 * @throws ResolvedControlThrowable  if the resumed call initiates a non-local control change
	 */
	void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable;

}
