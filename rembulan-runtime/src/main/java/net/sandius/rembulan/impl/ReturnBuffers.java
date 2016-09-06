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

package net.sandius.rembulan.impl;

import net.sandius.rembulan.ReturnBuffer;

/**
 * Static factory for instantiating return buffers.
 *
 * <p>To obtain a new instance of the default return buffer implementation,
 * use {@link ReturnBuffers#newDefaultReturnBuffer()}.</p>
 */
public final class ReturnBuffers {

	private ReturnBuffers() {
		// not to be instantiated
	}

	/**
	 * Returns a new instance of the default return buffer implementation.
	 *
	 * <p>This implementation optimises access to the first two values in the
	 * buffer.</p>
	 *
	 * @return  a new instance of the default return buffer
	 */
	public static ReturnBuffer newDefaultReturnBuffer() {
		return new PairCachingReturnBuffer();
	}

}
