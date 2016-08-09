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

import net.sandius.rembulan.util.Check;

abstract class CoroutineSwitch extends ControlThrowable {

	static final class Yield extends CoroutineSwitch {

		public final Object[] args;

		public Yield(Object[] args) {
			this.args = Check.notNull(args);
		}

	}

	static final class Resume extends CoroutineSwitch {

		public final Coroutine coroutine;
		public final Object[] args;

		public Resume(Coroutine coroutine, Object[] args) {
			this.coroutine = Check.notNull(coroutine);
			this.args = Check.notNull(args);
		}

	}

}
