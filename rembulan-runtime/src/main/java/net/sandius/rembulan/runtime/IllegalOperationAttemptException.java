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

import net.sandius.rembulan.LuaRuntimeException;

/**
 * An exception thrown by the runtime when an attempt to perform an illegal operation
 * is made.
 */
public class IllegalOperationAttemptException extends LuaRuntimeException {

	/**
	 * Constructs a new {@code IllegalOperationAttemptException} with the given error message.
	 *
	 * @param message  the error message
	 */
	public IllegalOperationAttemptException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@code IllegalOperationAttemptException} with {@code cause} as
	 * the cause of this error.
	 *
	 * @param cause  the cause of this exception, may be {@code null}
	 */
	public IllegalOperationAttemptException(Throwable cause) {
		super(cause);
	}

}
