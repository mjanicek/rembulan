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
	 * Callback triggered by the normal termination of the main function of the call
	 * with the identifier {@code id}.
	 *
	 * @param id  the call identifier, must not be {@code null}
	 * @param result  the return values, must not be {@code null}
	 */
	void returned(Object id, Object[] result);

	/**
	 * Callback triggered by an abnormal (error) termination of the main function of
	 * the call with the identifier {@code id}.
	 *
	 * @param id  the call identifier, must not be {@code null}
	 * @param error  the error that caused the abnormal termination, must not be {@code null}
	 */
	void failed(Object id, Throwable error);

	/**
	 * Callback triggered when the execution of the call with the identifier {@code id}
	 * is paused.
	 *
	 * @param id  the call identifier, must not be {@code null}
	 * @param cont  the call continuation, must not be {@code null}
	 */
	void paused(Object id, Continuation cont);

	/**
	 * Callback triggered by a request by the call with the identifier {@code id} to be
	 * resumed after the task {@code task} has been completed.
	 *
	 * @param id  the call identifier, must not be {@code null}
	 * @param cont  the call continuation, must not be {@code null}
	 * @param task  the task, must not be {@code null}
	 */
	void async(Object id, Continuation cont, AsyncTask task);

}

