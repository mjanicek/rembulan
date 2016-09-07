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

import net.sandius.rembulan.runtime.SchedulingContext;

/**
 * Reified, resumable state of computation.
 */
public interface Continuation {

	/**
	 * Returns the call identifier, an object that uniquely identifies the computation
	 * (an execution run) represented by this continuation.
	 *
	 * @return  a non-{@code null} identifier of this computation
	 */
	Object callIdentifier();

	/**
	 * Resumes the continuation with the given event handler {@code handler} in the scheduling
	 * context {@code schedulingContext}.
	 *
	 * @param handler  the call event handler, must not be {@code null}
	 * @param schedulingContext  the scheduling context, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code handler} or {@code schedulingContext}
	 *                               is {@code null}
	 * @throws InvalidContinuationException  when the continuation is invalid
	 */
	void resume(CallEventHandler handler, SchedulingContext schedulingContext);

}
