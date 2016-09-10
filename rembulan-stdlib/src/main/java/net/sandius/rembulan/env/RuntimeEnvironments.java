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

package net.sandius.rembulan.env;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Common implementations of runtime environments.
 */
public final class RuntimeEnvironments {

	private RuntimeEnvironments() {
		// not to be instantiated
	}

	/**
	 * Returns the system runtime environment.
	 *
	 * <p>Methods in this environment methods delegate to the system environment accessible
	 * by the Java Virtual Machine.</p>
	 *
	 * @return  the system runtime environment
	 */
	public static RuntimeEnvironment system() {
		return SystemRuntimeEnvironment.getInstance();
	}

	/**
	 * Returns the system runtime environment that uses the specified standard input,
	 * output and error streams.
	 *
	 * @param in  the standard input stream, may be {@code null}
	 * @param out  the standard output stream, may be {@code null}
	 * @param err  the standard error stream, may be {@code null}
	 * @return  a system runtime environment that uses the specified streams for its I/O
	 */
	public static RuntimeEnvironment system(InputStream in, OutputStream out, OutputStream err) {
		return new SystemRuntimeEnvironment(in, out, err);
	}

}
