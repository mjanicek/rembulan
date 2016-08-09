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

public abstract class ResumeResult {

	private ResumeResult() {
		// not to be instantiated by the outside
	}

	public static class Finished extends ResumeResult {

		public static final Finished INSTANCE = new Finished();

		private Finished() {
			// not to be instantiated by the outside
		}

	}

	public static class Error extends ResumeResult {

		public final Throwable error;

		public Error(Throwable error) {
			this.error = Check.notNull(error);
		}

	}

	public static class Pause extends ResumeResult {

		public static final Pause INSTANCE = new Pause();

		private Pause() {
			// not to be instantiated by the outside
		}

	}

	public static class WaitForAsync extends ResumeResult {

		public final Runnable task;

		public WaitForAsync(Runnable task) {
			this.task = task;
		}

	}

	public static class Switch extends ResumeResult {

		public final Coroutine target;

		public Switch(Coroutine target) {
			this.target = Check.notNull(target);
		}

	}

	public static class ImplicitYield extends ResumeResult {

		public final Coroutine target;
		public final Throwable error;

		public ImplicitYield(Coroutine target, Throwable error) {
			this.target = Check.notNull(target);
			this.error = error;
		}

	}

}
