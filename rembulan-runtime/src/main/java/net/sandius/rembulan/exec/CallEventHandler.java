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

package net.sandius.rembulan.exec;

import net.sandius.rembulan.AsyncTask;

/**
 * A callback object used to process control-related events during call execution.
 */
public interface CallEventHandler {

	/**
	 * The callback triggered by the normal termination of the main function of the call
	 * {@code c}.
	 *
	 * @param c  the call, must not be {@code null}
	 * @param result  the return values, must not be {@code null}
	 */
	void returned(Call c, Object[] result);

	/**
	 * The callback triggered by an abnormal (error) termination of the main function of
	 * the call {@code c}.
	 *
	 * @param c  the call, must not be {@code null}
	 * @param error  the error that caused the abnormal termination, must not be {@code null}
	 */
	void failed(Call c, Throwable error);

	/**
	 * The callback triggered when the execution of the call {@code c} is paused.
	 *
	 * @param c  the call, must not be {@code null}
	 * @param cont  the call continuation, must not be {@code null}
	 */
	void paused(Call c, OneShotContinuation cont);

	/**
	 * The callback triggered by a request by {@code c} to be resumed after the task
	 * {@code task} has been completed.
	 *
	 * @param c  the call, must not be {@code null}
	 * @param cont  the call continuation, must not be {@code null}
	 * @param task  the task, must not be {@code null}
	 */
	void async(Call c, OneShotContinuation cont, AsyncTask task);

}

