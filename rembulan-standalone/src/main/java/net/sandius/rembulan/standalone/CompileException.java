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

package net.sandius.rembulan.standalone;

import net.sandius.rembulan.parser.ParseException;
import net.sandius.rembulan.parser.Parser;
import net.sandius.rembulan.parser.TokenMgrError;

public class CompileException extends Exception {

	private final String source;

	public CompileException(String source, Throwable cause) {
		super(cause);
		this.source = source;
	}

	private static String exceptionMessage(Throwable ex) {
		if (ex instanceof TokenMgrError || ex instanceof ParseException) {
			return ex.getMessage();
		}
		else {
			return ex.getMessage() + " (" + ex.getClass().getName() + ")";
		}
	}

	private static int line(Throwable cause) {
		final int line;
		if (cause instanceof TokenMgrError) {
			return 0;  // TODO
		}
		else if (cause instanceof ParseException) {
			ParseException ex = (ParseException) cause;
			return ex.currentToken != null
					? ex.currentToken.beginLine
					: 0;
		}
		else {
			return 0;
		}
	}

	public String getLuaStyleErrorMessage() {
		Throwable cause = getCause();

		int line = line(cause);
		String prefix = source != null
				? source + ":" + (line > 0 ? Integer.toString(line) : "?") + ": "
				: "";

		return prefix + exceptionMessage(cause);
	}

	/**
	 * Returns {@code true} if this exception was caused by an unexpected EOF encountered
	 * by the lexer or parser.
	 *
	 * @return  {@code true} if the cause was an unexpected EOF
	 */
	public boolean isPartialInputError() {
		Throwable cause = getCause();
		if (cause instanceof TokenMgrError) {
			// TODO: is there really no better way?
			return cause.getMessage().contains("Encountered: <EOF>");
		}
		else if (cause instanceof ParseException) {
			ParseException ex = (ParseException) cause;
			return ex.currentToken != null
					&& ex.currentToken.next != null
					&& ex.currentToken.next.kind == Parser.EOF;
		}
		else {
			return false;
		}

	}

}
