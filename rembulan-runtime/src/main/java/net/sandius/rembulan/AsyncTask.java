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

package net.sandius.rembulan;

/**
 * An asynchronous task.
 *
 * <p>Asynchronous tasks are scheduled via {@link ExecutionContext#resumeAfter(AsyncTask)}.
 * The call executor executes the task (by calling {@link AsyncTask#execute(ContinueCallback)})
 * at some point after the task has been scheduled, and resumes the Lua function
 * call at some point after the task calls the method {@link ContinueCallback#finished()}
 * on the callback object supplied to it.</p>
 */
public interface AsyncTask {

	/**
	 * Executes the asynchronous task.
	 *
	 * <p>The task must call {@link ContinueCallback#finished()} on {@code callback} to
	 * notify the call executor that the task is finished. The {@code callback} object
	 * is supplied to the task by the executor; tasks may assume that {@code callback}
	 * is not {@code null}.</p>
	 *
	 * @param callback  continuation callback, to be called by the task once it is finished
	 */
	void execute(ContinueCallback callback);

	/**
	 * An interface for notifying the call executor that an asynchronous task is finished.
	 */
	interface ContinueCallback {

		/**
		 * Notifies the call executor that the asynchronous task this callback is attached
		 * to is finished.
		 *
		 * <p>The call executor may then resume the Lua function call that initiated
		 * the asynchronous task. Subsequent calls of this method (i.e., after it has been
		 * called once) have no effect.</p>
		 */
		void finished();

	}

}
