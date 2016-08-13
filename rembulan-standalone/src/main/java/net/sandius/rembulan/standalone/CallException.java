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

import java.io.PrintStream;

public class CallException extends Exception {

	public CallException(Throwable cause) {
		super(cause);
	}

	private static boolean isSuppressed(StackTraceElement elem, String[] suppressedClassPrefixes) {
		if (elem != null && suppressedClassPrefixes != null) {
			String className = elem.getClassName();
			for (String prefix : suppressedClassPrefixes) {
				if (className.startsWith(prefix)) {
					return true;
				}
			}
		}

		return false;
	}

	public void printLuaFormatStackTraceback(PrintStream stream, boolean relative, String[] suppress) {
		Throwable cause = getCause();

		stream.println(cause.getMessage());
		stream.println("stack traceback:");

		StackTraceElement[] causeStackTrace = cause.getStackTrace();
		StackTraceElement[] currentStackTrace = Thread.currentThread().getStackTrace();

		int numOmitted = 0;
		if (relative) {
			// find common suffix length
			int i;
			for (i = 0; i < Math.min(causeStackTrace.length, currentStackTrace.length); i++) {
				StackTraceElement cau = causeStackTrace[causeStackTrace.length - 1 - i];
				StackTraceElement cur = currentStackTrace[currentStackTrace.length - 1 - i];
				if (!cau.equals(cur)) {
					break;
				}
			}
			numOmitted = i;
		}

		int suppressedSince = 0;
		for (int i = 0; i < causeStackTrace.length - numOmitted; i++) {
			StackTraceElement elem = causeStackTrace[i];

			if (isSuppressed(elem, suppress)) {
				suppressedSince += 1;
			}
			else {
				if (suppressedSince > 0) {
					stream.println("\t[Java]: (" + suppressedSince + " suppressed)");
					suppressedSince = 0;
				}
				stream.print("\t[Java]: at ");
				stream.println(elem);
			}
		}

		if (numOmitted > 0 || suppressedSince > 0) {
			stream.print("\t[Java]: (");
			if (suppressedSince > 0) {
				stream.print(suppressedSince + " suppressed");
				if (numOmitted > 0) {
					stream.print(", ");
				}
			}
			stream.print(numOmitted + " omitted");
			stream.println(")");
		}
	}

}
