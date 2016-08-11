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

package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;

public abstract class CoroutineLib extends Lib {

	@Override
	public void installInto(LuaState state, Table env) {
		Table t = state.newTable();
		env.rawset("coroutine", t);

		t.rawset("create", _create());
		t.rawset("resume", _resume());
		t.rawset("yield", _yield());
		t.rawset("isyieldable", _isyieldable());
		t.rawset("status", _status());
		t.rawset("running", _running());
		t.rawset("wrap", _wrap());
	}

	/**
	 * {@code coroutine.create (f)}
	 *
	 * <p>Creates a new coroutine, with body {@code f}. {@code f} must be a function.
	 * Returns this new coroutine, an object with type {@code "thread"}.</p>
	 *
	 * @return the {@code coroutine.create} function
	 */
	protected abstract Function _create();

	/**
	 * {@code coroutine.resume (co [, val1, ···])}
	 *
	 * <p>Starts or continues the execution of coroutine {@code co}. The first time you resume
	 * a coroutine, it starts running its body. The values {@code val1}, ... are passed
	 * as the arguments to the body function. If the coroutine has yielded, {@code resume} restarts it;
	 * the values {@code val1}, ... are passed as the results from the yield.</p>
	 *
	 * <p>If the coroutine runs without any errors, {@code resume} returns <b>true</b> plus
	 * any values passed to {@code yield} (when the coroutine yields) or any values returned
	 * by the body function (when the coroutine terminates). If there is any error, {@code resume}
	 * returns <b>false</b> plus the error message.</p>
	 *
	 * @return the {@code coroutine.resume} function
	 */
	protected abstract Function _resume();

	/**
	 * {@code coroutine.yield (···)}
	 *
	 * <p>Suspends the execution of the calling coroutine. Any arguments to {@code yield}
	 * are passed as extra results to {@code resume}.</p>
	 *
	 * @return the {@code coroutine.yield} function
	 */
	protected abstract Function _yield();

	/**
	 * {@code coroutine.isyieldable ()}
	 *
	 * <p>Returns <b>true</b> when the running coroutine can yield.</p>
	 *
	 * <p>A running coroutine is yieldable if it is not the main thread and it is not inside
	 * a non-yieldable C function.</p>
	 *
	 * @return the {@code coroutine.isyieldable} function
	 */
	protected abstract Function _isyieldable();

	/**
	 * {@code coroutine.status (co)}
	 *
	 * <p>Returns the status of coroutine {@code co}, as a string: {@code "running"},
	 * if the coroutine is running (that is, it called {@code status}); {@code "suspended"},
	 * if the coroutine is suspended in a call to {@code yield}, or if it has not started
	 * running yet; {@code "normal"} if the coroutine is active but not running (that is,
	 * it has resumed another coroutine); and {@code "dead"} if the coroutine has finished
	 * its body function, or if it has stopped with an error.</p>
	 *
	 * @return the {@code coroutine.status} function
	 */
	protected abstract Function _status();

	/**
	 * {@code coroutine.running ()}
	 *
	 * <p>Returns the running coroutine plus a boolean, <b>true</b> when the running coroutine
	 * is the main one.</p>
	 *
	 * @return the {@code coroutine.running} function
	 */
	protected abstract Function _running();

	/**
	 * {@code coroutine.wrap (f)}
	 *
	 * <p>Creates a new coroutine, with body {@code f}. {@code f} must be a function.
	 * Returns a function that resumes the coroutine each time it is called. Any arguments
	 * passed to the function behave as the extra arguments to {@code resume}.
	 * Returns the same values returned by {@code resume}, except the first boolean. In case
	 * of error, propagates the error.</p>
	 *
	 * @return the {@code coroutine.wrap} function
	 */
	protected abstract Function _wrap();

}
