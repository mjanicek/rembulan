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

package net.sandius.rembulan.core.exec;

import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.load.ChunkClassLoader;
import net.sandius.rembulan.util.Check;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

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

	private static boolean isLuaClass(ChunkClassLoader chunkClassLoader, String className) {
		return chunkClassLoader != null
				&& className != null
				&& chunkClassLoader.isInstalled(className);
	}

	private static String luaTracebackElementString(String fileName, int line, String className) {
		return (fileName != null ? fileName : "?")
				+  ":"
				+ (line >= 0 ? line : "?")
				+ ": "
				+ "in function <" + Check.notNull(className) + ">";
	}

	private static String suppressedOrOmittedString(int suppressed, int omitted) {
		if (suppressed > 0 || omitted > 0) {
			StringBuilder bld = new StringBuilder();

			bld.append("[Java]: (");
			if (suppressed > 0) {
				bld.append(suppressed).append(" suppressed");
				if (omitted > 0) {
					bld.append(", ");
				}
			}
			if (omitted > 0) {
				bld.append(omitted).append(" omitted");
			}
			bld.append(")");
			return bld.toString();
		}
		else {
			return null;
		}
	}

	String getLuaFormatStackTraceback(ChunkClassLoader chunkClassLoader, boolean relative, String[] suppress) {
		Throwable cause = getCause();

		StringBuilder bld = new StringBuilder();
		bld.append(cause.getMessage()).append('\n');
		bld.append("stack traceback:\n");

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

		Deque<String> lines = new ArrayDeque<>();

		int suppressedSince = 0;
		int omittedSince = numOmitted;

		for (int i = causeStackTrace.length - 1 - numOmitted; i >= 0; i--) {

			StackTraceElement elem = causeStackTrace[i];

			if (elem.getClassName().equals(Dispatch.class.getName())) {
				if (elem.getMethodName().equals("evaluateTailCalls")) {
					// FIXME: remove code duplication
					String s = suppressedOrOmittedString(suppressedSince, omittedSince);
					if (s != null) {
						lines.push(s);
						suppressedSince = 0;
						omittedSince = 0;
					}

					lines.push("(...tail calls...)");
				}
			}
			else if (isSuppressed(elem, suppress)) {
				suppressedSince += 1;
			}
			else {
				String s = suppressedOrOmittedString(suppressedSince, omittedSince);
				if (s != null) {
					lines.push(s);
					suppressedSince = 0;
					omittedSince = 0;
				}

				String className = elem.getClassName();
				if (isLuaClass(chunkClassLoader, className)
						&& (elem.getMethodName().equals("invoke") || elem.getMethodName().equals("resume"))) {

					String fileName = elem.getFileName();
					int line = elem.getLineNumber();

					do {
						int j = i - 1;
						if (j >= 0) {
							StackTraceElement nextElem = causeStackTrace[j];
							if (Objects.equals(className, nextElem.getClassName())
									&& Objects.equals(fileName, nextElem.getFileName())) {
								// it's the same function

								i -= 1;  // skip the next element in the stack trace

								int l = nextElem.getLineNumber();
								if (l >= 0) {
									line = l;
								}
							}
							else {
								break;
							}
						}
					} while (i >= 0);

					lines.push(luaTracebackElementString(fileName, line, className));
				}
				else {
					lines.push("[Java]: at " + elem.toString());
				}
			}

		}

		String s = suppressedOrOmittedString(suppressedSince, omittedSince);
		if (s != null) {
			lines.push(s);
		}

		for (String line : lines) {
			bld.append('\t').append(line).append('\n');
		}

		return bld.toString();
	}

	public void printLuaFormatStackTraceback(
			PrintStream stream,
			ChunkClassLoader chunkClassLoader,
			boolean relative,
			String[] suppress) {

		stream.print(getLuaFormatStackTraceback(chunkClassLoader, relative, suppress));
	}

}
