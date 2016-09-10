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

import java.util.Objects;

/**
 * An exception thrown by a call executor when a call is paused. The continuation
 * of the paused call may be accessed using the method {@link #getContinuation()}.
 *
 * <p>For performance reasons, instances of this class do not contain a stack trace.</p>
 */
public class CallPausedException extends Exception {

	private final Continuation continuation;

	/**
	 * Constructs a new instance of {@code CallPausedException} with the specified
	 * continuation.
	 *
	 * <p>For performance reasons, the exception does not have a writable stack trace.</p>
	 *
	 * @param continuation  the continuation of the paused call, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code continuation} is {@code null}
	 */
	public CallPausedException(Continuation continuation) {
		super("Call paused", null, true, false);
		this.continuation = Objects.requireNonNull(continuation);
	}

	/**
	 * Get the continuation of the paused call.
	 *
	 * @return  the call continuation
	 */
	public Continuation getContinuation() {
		return continuation;
	}

}
