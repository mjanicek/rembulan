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

import net.sandius.rembulan.Function;

/**
 * An interface to the execution context of a Lua call.
 *
 * <p>This is the interface to the runtime environment in which Lua calls are executed,
 * providing the mechanisms for
 * including coroutine switching and the (optional) cooperative scheduler.</p>
 *
 * <p><b>Note</b>: Lua functions are <b>not</b> guaranteed to receive the same
 * {@code ExecutionContext} instance between an invoke and a subsequent resume.
 * Functions should therefore not retain the reference to the {@code ExecutionContext}
 * that outlives an {@code invoke} or {@code resume}.</p>
 *
 * <p><b>Note</b>: The behaviour of the methods in this interface is <i>undefined</i> when
 * invoked outside a Lua function invoke/resume.</p>
 */
public interface ExecutionContext {

	/**
	 * Returns the Lua state used in this execution context.
	 *
	 * @return  the Lua state used in this execution context
	 */
	@SuppressWarnings("unused")
	LuaState getState();

	/**
	 * Returns the return buffer used in this execution context.
	 *
	 * <p>This is the mechanism by which Lua functions may return values (see e.g.
	 * {@link ReturnBuffer#setToContentsOf(Object[])}), or indicate that the return
	 * value is the result of a tail call (by e.g.
	 * {@link ReturnBuffer#setToCallWithContentsOf(Object, Object[])}).</p>
	 *
	 * <p>The return values of Lua calls initiated from this execution context will
	 * also be stored in a return buffer accessible by this method (e.g. by
	 * {@link ReturnBuffer#getAsArray()} or {@link ReturnBuffer#get(int)}).</p>
	 *
	 * <p><b>Note</b>: As with {@link ExecutionContext}, the return buffer instance may
	 * change between subsequent invokes and resumes of the same function. The reference
	 * to the {@code ReturnBuffer} instance should therefore not be retained by the executed
	 * function beyond the scope of a single invoke or resume.</p>
	 *
	 * @return  the return buffer used in this execution context
	 */
	@SuppressWarnings("unused")
	ReturnBuffer getReturnBuffer();

	/**
	 * Returns the current coroutine.
	 *
	 * @return  the current coroutine
	 */
	@SuppressWarnings("unused")
	Coroutine getCurrentCoroutine();

	/**
	 * Returns {@code true} if the current coroutine (as returned
	 * by {@link #getCurrentCoroutine()}) is the main coroutine.
	 *
	 * @return  {@code true} if the current coroutine is the main coroutine
	 */
	@SuppressWarnings("unused")
	boolean isInMainCoroutine();

	/**
	 * Returns a new coroutine with the body {@code function}.
	 *
	 * <p>The coroutine will be initialised in a suspended state. To resume the coroutine,
	 * use {@link #resume(Coroutine, Object[])}.</p>
	 *
	 * @param function  coroutine body, must not be {@code null}
	 * @return  a new (suspended) coroutine with the body {@code function}
	 */
	@SuppressWarnings("unused")
	Coroutine newCoroutine(Function function);

	/**
	 * Resumes the given coroutine {@code coroutine}, passing the arguments {@code args}
	 * to it.
	 * <b>This method throws a {@link ControlThrowable}</b>; the throwable should be caught,
	 * handled and re-thrown by the caller of this method.
	 *
	 * <p>The reference to the array {@code args} is not retained by the execution context;
	 * {@code args} may therefore be freely re-used by the caller.</p>
	 *
	 * @param coroutine  coroutine to be resumed, must not be {@code null}
	 * @param args  arguments to be passed to {@code coroutine}, must not be {@code null}
	 *
	 * @throws ControlThrowable  the control throwable for this coroutine switch
	 */
	@SuppressWarnings("unused")
	void resume(Coroutine coroutine, Object[] args) throws ControlThrowable;

	/**
	 * Yields control to the coroutine resuming the current coroutine, passing the
	 * arguments {@code args} to it.
	 * <b>This method throws a {@link ControlThrowable}</b>; the throwable should be caught,
	 * handled and re-thrown by the caller of this method.
	 *
	 * <p>The reference to the array {@code args} is not retained by the execution context;
	 * {@code args} may therefore be freely re-used by the caller.</p>
	 *
	 * @param args  arguments to be passed to the resuming coroutine, must not be {@code null}
	 *
	 * @throws ControlThrowable  the control throwable for this coroutine switch
	 */
	@SuppressWarnings("unused")
	void yield(Object[] args) throws ControlThrowable;

	/**
	 * Resumes the current call after the asynchronous task {@code task} has been completed.
	 * <b>This method throws a {@link ControlThrowable}</b>; the throwable should be caught,
	 * handled and re-thrown by the caller of this method.
	 *
	 * <p>In order to mark {@code task} as completed, the task must call
	 * {@link ContinueCallback#finished()}.</p>
	 *
	 * @param task  the task to be executed, must not be {@code null}
	 *
	 * @throws ControlThrowable  the control throwable for this control change
	 */
	@SuppressWarnings("unused")
	void resumeAfter(AsyncTask task) throws ControlThrowable;

	/**
	 * Informs the scheduler that the current task is about to consume or has consumed
	 * {@code ticks} virtual ticks.
	 *
	 * @param ticks  number of ticks to be registered with the scheduler
	 */
	@SuppressWarnings("unused")
	void registerTicks(int ticks);

	/**
	 * Pauses the execution if the according to the scheduler this call should be paused.
	 * <b>This method throws a {@link ControlThrowable}</b>; the throwable should be caught,
	 * handled and re-thrown by the caller of this method.
	 *
	 * <p>To pause execution unconditionally, use {@link #pause()}.</p>
	 *
	 * @throws ControlThrowable  the control throwable for this control change
	 */
	@SuppressWarnings("unused")
	void checkCallYield() throws ControlThrowable;

	/**
	 * (Unconditionally) pauses the execution.
	 * <b>This method throws a {@link ControlThrowable}</b>; the throwable should be caught,
	 * handled and re-thrown by the caller of this method.
	 *
	 * @throws ControlThrowable  the control throwable for this control change
	 */
	@SuppressWarnings("unused")
	void pause() throws ControlThrowable;

}
