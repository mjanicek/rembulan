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

/**
 * An exception thrown when loading Lua chunks to indicate that the input cannot be parsed,
 * compiled or loaded as a function.
 */
public class LoaderException extends Exception {

	private final String sourceFileName;
	private final int sourceLine;
	private final boolean partialInput;

	/**
	 * Constructs a new {@code LoaderException} with the specified {@code cause},
	 * the file name {@code sourceFileName}, the line {@code sourceLine} where the error
	 * occurred, and indicating in the flag {@code partialInput} whether the cause of the
	 * error may have been caused by a partial input (i.e., an unexpected EOF).
	 *
	 * @param cause  the cause of the error, must not be {@code null}
	 * @param sourceFileName  source file name, may be {@code null} when source file information
	 *                        is omitted
	 * @param sourceLine  the line in the source where the error occurred, may be non-positive
	 *                    when this information is omitted
	 * @param partialInput  a flag indicating whether the cause of the error may have been
	 *                      an unexpected EOF
	 *
	 * @throws NullPointerException  if {@code cause} is {@code null}
	 */
	public LoaderException(Throwable cause, String sourceFileName, int sourceLine, boolean partialInput) {
		super(cause);
		this.sourceFileName = sourceFileName;  // may be null
		this.sourceLine = sourceLine;
		this.partialInput = partialInput;
	}

	/**
	 * Constructs a new {@code LoaderException} without line information or the indication
	 * that the cause of this error may have been a partial input.
	 *
	 * @param cause  the cause of the error, must not be {@code null}
	 * @param sourceFileName  source file name, may be {@code null} when source file information
	 *                        is omitted
	 *
	 * @throws NullPointerException  if {@code cause} is {@code null}
	 */
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

	/**
	 * Returns a Lua-style error message for this loader exception.
	 *
	 * <p>The format of this error message is:</p>
	 * <pre>
	 *     "SourceFileName:SourceLine: CauseClass: CauseMessage"
	 * </pre>
	 * <p>When no source file name is available, the source information prefix is omitted;
	 * when no source line information is available, the {@code SourceLine} string is
	 * equal to "?".</p>
	 *
	 * @return  a Lua-style error message for this loader exception
	 */
	public String getLuaStyleErrorMessage() {
		Throwable cause = getCause();

		String prefix = sourceFileName != null
				? sourceFileName + ":" + (sourceLine > 0 ? Integer.toString(sourceLine) : "?") + ": "
				: "";

		return prefix + cause.getClass().getName() + ": " + cause.getMessage();
	}

}
