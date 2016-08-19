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

package net.sandius.rembulan.core;

import net.sandius.rembulan.core.load.ChunkClassLoader;
import net.sandius.rembulan.util.Check;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;

public class StackTraceback {

	public static final Entry TAIL_CALLS = MiscEntry.fromString("...tail calls...");

	private static final String JAVA_PREFIX = "[Java]: ";

	private final Entry[] entries;

	StackTraceback(Entry[] entries) {
		this.entries = Check.notNull(entries);
	}

	public static StackTraceback fromCollection(Collection<Entry> entries) {
		return new StackTraceback(Check.notNull(entries.toArray(new Entry[entries.size()])));
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		bld.append("stack traceback:\n");
		for (Entry e : entries) {
			bld.append('\t').append(e.toString()).append('\n');
		}
		return bld.toString();
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

	public static StackTraceback getStackTraceback(Throwable throwable, StackTraceElement[] currentStackTrace, ChunkClassLoader chunkClassLoader, String[] suppress) {

		Deque<Entry> entries = new ArrayDeque<>();

		StackTraceElement[] causeStackTrace = throwable.getStackTrace();

		int numOmitted = 0;
		if (currentStackTrace != null) {
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
		int omittedSince = numOmitted;

		for (int i = causeStackTrace.length - 1 - numOmitted; i >= 0; i--) {

			StackTraceElement elem = causeStackTrace[i];

			if (elem.getClassName().equals(Dispatch.class.getName())) {
				if (elem.getMethodName().equals("evaluateTailCalls")) {
					// FIXME: remove code duplication
					if (suppressedSince > 0 || omittedSince > 0) {
						entries.push(StackTraceback.MiscJavaEntry.suppressedOrOmitted(suppressedSince, omittedSince));
						suppressedSince = 0;
						omittedSince = 0;
					}

					entries.push(StackTraceback.TAIL_CALLS);
				}
			}
			else if (isSuppressed(elem, suppress)) {
				suppressedSince += 1;
			}
			else {
				if (suppressedSince > 0 || omittedSince > 0) {
					entries.push(StackTraceback.MiscJavaEntry.suppressedOrOmitted(suppressedSince, omittedSince));
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

					entries.push(StackTraceback.LuaCallEntry.of(fileName, line, className));
				}
				else {
					entries.push(StackTraceback.JavaCallEntry.fromStackTraceElement(elem));
				}
			}

		}

		if (suppressedSince > 0 || omittedSince > 0) {
			entries.push(StackTraceback.MiscJavaEntry.suppressedOrOmitted(suppressedSince, omittedSince));
		}

		return StackTraceback.fromCollection(entries);
	}


	public static abstract class Entry {

	}

	public static class JavaCallEntry extends Entry {

		private final StackTraceElement stackTraceElement;

		JavaCallEntry(StackTraceElement stackTraceElement) {
			this.stackTraceElement = Check.notNull(stackTraceElement);
		}

		public static JavaCallEntry fromStackTraceElement(StackTraceElement element) {
			return new JavaCallEntry(element);
		}

		@Override
		public String toString() {
			return JAVA_PREFIX + stackTraceElement.toString();
		}

	}

	public static class MiscJavaEntry extends Entry {

		private final String s;

		MiscJavaEntry(String s) {
			this.s = Check.notNull(s);
		}

		public static MiscJavaEntry suppressedOrOmitted(int suppressed, int omitted) {
			if (suppressed > 0 || omitted > 0) {
				StringBuilder bld = new StringBuilder();

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

				return new MiscJavaEntry(bld.toString());
			}
			else {
				throw new IllegalArgumentException("suppressed or omitted must be positive (got "
						+ suppressed + " and " + omitted + ")");
			}
		}

		@Override
		public String toString() {
			return JAVA_PREFIX + "(" + s + ")";
		}

	}

	public static class LuaCallEntry extends Entry {

		private final String sourceFileName;  // may be null
		private final int sourceLine;
		private final String functionName;

		LuaCallEntry(String sourceFileName, int sourceLine, String functionName) {
			this.sourceFileName = sourceFileName;  // may be null
			this.sourceLine = sourceLine;
			this.functionName = Check.notNull(functionName);
		}

		public static LuaCallEntry of(String sourceFileName, int sourceLine, String functionName) {
			return new LuaCallEntry(sourceFileName, sourceLine, functionName);
		}

		@Override
		public String toString() {
			return (sourceFileName != null ? sourceFileName : "?")
					+  ":"
					+ (sourceLine >= 0 ? sourceLine : "?")
					+ ": "
					+ "in function <" + Check.notNull(functionName) + ">";
		}

	}

	public static class MiscEntry extends Entry {

		private final String s;

		MiscEntry(String s) {
			this.s = Check.notNull(s);
		}

		public static MiscEntry fromString(String s) {
			return new MiscEntry(s);
		}

		@Override
		public String toString() {
			return "(" + s + ")";
		}

	}

}
