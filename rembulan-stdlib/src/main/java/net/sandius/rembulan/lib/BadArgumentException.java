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

package net.sandius.rembulan.lib;

/**
 * Thrown to indicate that an illegal argument was passed to a library function.
 */
public class BadArgumentException extends IllegalArgumentException {

	private final int idx;
	private final String name;
	private final String message;

	/**
	 * Constructs a new {@code BadArgumentException} indicating that the argument
	 * {@code argumentIndex} passed to the function {@code functionName} is illegal,
	 * with the error message {@code message}.
	 *
	 * <p>For consistency with PUC-Lua error messages, {@code argumentIndex} should
	 * be 1-based.</p>
	 *
	 * <p>If {@code functionName} is {@code null}, {@code "?"} will be used in the error
	 * message. {@code message} may be {@code null}, in which case no error details
	 * will be included by this exception's {@link #getMessage()}.</p>
	 *
	 * @param argumentIndex  the (1-based) index of the illegal argument
	 * @param functionName  the name of the function, may be {@code null}
	 * @param message  the error details, may be {@code null}
	 */
	public BadArgumentException(int argumentIndex, String functionName, String message) {
		this.idx = argumentIndex;
		this.name = functionName;
		this.message = message;
	}

	/**
	 * Constructs a new {@code BadArgumentException} indicating that the argument
	 * {@code argumentIndex} passed to the function {@code functionName} is illegal,
	 * with {@code cause} containing the cause of the error.
	 *
	 * <p>For consistency with PUC-Lua error messages, {@code argumentIndex} should
	 * be 1-based.</p>
	 *
	 * <p>If {@code functionName} is {@code null}, {@code "?"} will be used in the error
	 * message. {@code cause} is {@code null} or {@code cause.getMessage()} returns
	 * {@code null}, then no error details will be included in the error message returned by
	 * this exception's {@link #getMessage()}. Otherwise, {@code cause.getMessage()}
	 * will be used to provide the error details.</p>
	 *
	 * <p>Furthermore, {@code cause} will be accessible via {@link #getCause()}.</p>
	 *
	 * @param argumentIndex  the (1-based) index of the illegal argument
	 * @param functionName  the name of the function, may be {@code null}
	 * @param cause  the cause, may be {@code null}
	 */
	public BadArgumentException(int argumentIndex, String functionName, Throwable cause) {
		super(cause);
		this.idx = argumentIndex;
		this.name = functionName;
		this.message = null;
	}

	@Override
	public String getMessage() {
		StringBuilder bld = new StringBuilder();
		bld.append("bad argument #").append(idx).append(" to '");
		bld.append(name != null ? name : "?");
		bld.append("'");

		String msg = message;
		if (msg == null && getCause() != null) {
			msg = getCause().getMessage();
		}

		if (msg != null) {
			bld.append(" (").append(msg).append(")");
		}

		return bld.toString();
	}

}
