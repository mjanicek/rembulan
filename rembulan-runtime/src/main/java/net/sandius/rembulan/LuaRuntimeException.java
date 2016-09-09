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

/**
 * Base class for runtime exceptions that carry arbitrary <i>error objects</i>
 * attached to them.
 *
 * <p>To retrieve the error object, use {@link #getErrorObject()}.</p>
 */
public class LuaRuntimeException extends RuntimeException {

	private final Object errorObject;

	private LuaRuntimeException(Throwable cause, Object errorObject) {
		super(cause);
		this.errorObject = errorObject;
	}

	/**
	 * Constructs a new {@code LuaRuntimeException} with {@code errorObject} as its
	 * error object. {@code errorObject} may be {@code null}.
	 *
	 * @param errorObject  the error object, may be {@code null}
	 */
	public LuaRuntimeException(Object errorObject) {
		this(null, errorObject);
	}

	/**
	 * Constructs a new {@code LuaRuntimeException} with {@code cause} as its cause.
	 *
	 * <p>When queried for the error object, invokes {@link Conversions#toErrorObject(Throwable)}
	 * on {@code cause}; when {@code cause} is {@code null}, then the error object
	 * is {@code null}.</p>
	 *
	 * @param cause  the cause of this error, may be {@code null}
	 */
	public LuaRuntimeException(Throwable cause) {
		this(cause, null);
	}

	/**
	 * Returns the error object attached to this exception converted to a string.
	 *
	 * @return  error object converted to a string
	 */
	@Override
	public String getMessage() {
		return Conversions.toHumanReadableString(getErrorObject());
	}

	/**
	 * Returns the error object attached to this exception. The error object may be {@code null}.
	 *
	 * @return  the error object attached to this exception (possibly {@code null})
	 */
	public Object getErrorObject() {
		Throwable cause = getCause();
		if (cause != null) {
			return Conversions.toErrorObject(cause);
		}
		else {
			return errorObject;
		}
	}

}
