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

import net.sandius.rembulan.core.Call;
import net.sandius.rembulan.util.Check;

public class CallInterruptedException extends InterruptedException {

	private final Call.Continuation continuation;

	CallInterruptedException(Call.Continuation continuation) {
		super("Call interrupted");
		this.continuation = Check.notNull(continuation);
	}

	public Call.Continuation getContinuation() {
		return continuation;
	}

}
