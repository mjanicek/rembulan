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

package net.sandius.rembulan.load;

public class LoaderException extends Exception {

	private final String sourceFileName;
	private final int sourceLine;
	private final boolean partialInput;

	public LoaderException(Throwable cause, String sourceFileName, int sourceLine, boolean partialInput) {
		super(cause);
		this.sourceFileName = sourceFileName;  // may be null
		this.sourceLine = sourceLine;
		this.partialInput = partialInput;
	}

	public LoaderException(Throwable cause, String sourceFileName) {
		this(cause, sourceFileName, 0, false);
	}

	/**
	 * Returns {@code true} if this {@code LoaderException} was caused by an unexpected
	 * EOF when processing the Lua program.
	 *
	 * @return  {@code true} if this exception was caused by an unexpected EOF
	 */
	public boolean isPartialInputError() {
		return partialInput;
	}

	/**
	 * Returns the file name of the program that caused this exception. May return {@code null}
	 * if this information is not available.
	 *
	 * @return  file name of the program, or {@code null} when this information
	 *          is not available
	 */
	public String getSourceFileName() {
		return sourceFileName;
	}

	/**
	 * Returns the line in the source file that caused the exception. May return a number
	 * lesser than 1 when this information is not available.
	 *
	 * @return  line that caused this error, or a number lesser than 1 if this information
	 *          is not available
	 */
	public int getSourceLine() {
		return sourceLine;
	}

	public String getLuaStyleErrorMessage() {
		Throwable cause = getCause();

		String prefix = sourceFileName != null
				? sourceFileName + ":" + (sourceLine > 0 ? Integer.toString(sourceLine) : "?") + ": "
				: "";

		return prefix + cause.getClass().getName() + ": " + cause.getMessage();
	}

}
