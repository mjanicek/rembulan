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
 *
 * --
 * Portions of this file are licensed under the Lua license. For Lua
 * licensing details, please visit
 *
 *     http://www.lua.org/license.html
 *
 * Copyright (C) 1994-2016 Lua.org, PUC-Rio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.sandius.rembulan.lib;

import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.runtime.AbstractFunctionAnyArg;
import net.sandius.rembulan.runtime.Coroutine;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ProtectedResumable;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.ReturnBuffer;
import net.sandius.rembulan.runtime.UnresolvedControlThrowable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * This library comprises the operations to manipulate coroutines, which come inside the table
 * {@code coroutine}. See §2.6 of the Lua Reference Manual for a general description of coroutines.
 */
public final class CoroutineLib {

	static final LuaFunction CREATE = new Create();
	static final LuaFunction ISYIELDABLE = new IsYieldable();
	static final LuaFunction RESUME = new Resume();
	static final LuaFunction RUNNING = new Running();
	static final LuaFunction STATUS = new Status();
	static final LuaFunction WRAP = new Wrap();
	static final LuaFunction YIELD = new Yield();

	/**
	 * Returns the function {@code coroutine.create}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code coroutine.create (f)}
	 *
	 * <p>Creates a new coroutine, with body {@code f}. {@code f} must be a function.
	 * Returns this new coroutine, an object with type {@code "thread"}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code coroutine.create} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.create">
	 *     the Lua 5.3 Reference Manual entry for <code>coroutine.create</code></a>
	 */
	public static LuaFunction create() {
		return CREATE;
	}

	/**
	 * Returns the function {@code coroutine.isyieldable}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code coroutine.isyieldable ()}
	 *
	 * <p>Returns <b>true</b> when the running coroutine can yield.</p>
	 *
	 * <p>A running coroutine is yieldable if it is not the main thread and it is not inside
	 * a non-yieldable C function.</p>
	 * </blockquote>
	 *
	 * @return  the {@code coroutine.isyieldable} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.isyieldable">
	 *     the Lua 5.3 Reference Manual entry for <code>coroutine.isyieldable</code></a>
	 */
	public static LuaFunction isyieldable() {
		return ISYIELDABLE;
	}

	/**
	 * Returns the function {@code coroutine.resume}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
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
	 * </blockquote>
	 *
	 * @return  the {@code coroutine.resume} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.resume">
	 *     the Lua 5.3 Reference Manual entry for <code>coroutine.resume</code></a>
	 */
	public static LuaFunction resume() {
		return RESUME;
	}

	/**
	 * Returns the function {@code coroutine.running}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code coroutine.running ()}
	 *
	 * <p>Returns the running coroutine plus a boolean, <b>true</b> when the running coroutine
	 * is the main one.</p>
	 * </blockquote>
	 *
	 * @return  the {@code coroutine.running} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.running">
	 *     the Lua 5.3 Reference Manual entry for <code>coroutine.running</code></a>
	 */
	public static LuaFunction running() {
		return RUNNING;
	}

	/**
	 * Returns the function {@code coroutine.status}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code coroutine.status (co)}
	 *
	 * <p>Returns the status of coroutine {@code co}, as a string: {@code "running"},
	 * if the coroutine is running (that is, it called {@code status}); {@code "suspended"},
	 * if the coroutine is suspended in a call to {@code yield}, or if it has not started
	 * running yet; {@code "normal"} if the coroutine is active but not running (that is,
	 * it has resumed another coroutine); and {@code "dead"} if the coroutine has finished
	 * its body function, or if it has stopped with an error.</p>
	 * </blockquote>
	 *
	 * @return  the {@code coroutine.status} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.status">
	 *     the Lua 5.3 Reference Manual entry for <code>coroutine.status</code></a>
	 */
	public static LuaFunction status() {
		return STATUS;
	}

	/**
	 * Returns the function {@code coroutine.wrap}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code coroutine.wrap (f)}
	 *
	 * <p>Creates a new coroutine, with body {@code f}. {@code f} must be a function.
	 * Returns a function that resumes the coroutine each time it is called. Any arguments
	 * passed to the function behave as the extra arguments to {@code resume}.
	 * Returns the same values returned by {@code resume}, except the first boolean. In case
	 * of error, propagates the error.</p>
	 * </blockquote>
	 *
	 * @return  the {@code coroutine.wrap} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.wrap">
	 *     the Lua 5.3 Reference Manual entry for <code>coroutine.wrap</code></a>
	 */
	public static LuaFunction wrap() {
		return WRAP;
	}

	/**
	 * Returns the function {@code coroutine.yield}.
	 *
	 * <p>The following is the corresponding entry from the Lua Reference Manual:</p>
	 *
	 * <blockquote>
	 * {@code coroutine.yield (···)}
	 *
	 * <p>Suspends the execution of the calling coroutine. Any arguments to {@code yield}
	 * are passed as extra results to {@code resume}.</p>
	 * </blockquote>
	 *
	 * @return  the {@code coroutine.yield} function
	 *
	 * @see <a href="http://www.lua.org/manual/5.3/manual.html#pdf-coroutine.yield">
	 *     the Lua 5.3 Reference Manual entry for <code>coroutine.yield</code></a>
	 */
	public static LuaFunction yield() {
		return YIELD;
	}


	private CoroutineLib() {
		// not to be instantiated
	}

	/**
	 * Installs the coroutine library to the global environment {@code env} in the state
	 * context {@code context}.
	 *
	 * <p>If {@code env.package.loaded} is a table, adds the library table
	 * to it with the key {@code "coroutine"}, using raw access.</p>
	 *
	 * @param context  the state context, must not be {@code null}
	 * @param env  the global environment, must not be {@code null}
	 *
	 * @throws NullPointerException  if {@code context} or {@code env} is {@code null}
	 */
	public static void installInto(StateContext context, Table env) {
		Objects.requireNonNull(context);
		Objects.requireNonNull(env);

		Table t = context.newTable();

		t.rawset("create", create());
		t.rawset("resume", resume());
		t.rawset("yield", yield());
		t.rawset("isyieldable", isyieldable());
		t.rawset("status", status());
		t.rawset("running", running());
		t.rawset("wrap", wrap());

		ModuleLib.install(env, "coroutine", t);
	}

	static class Create extends AbstractLibFunction {

		@Override
		protected String name() {
			return "create";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			LuaFunction func = args.nextFunction();
			Coroutine c = context.newCoroutine(func);
			context.getReturnBuffer().setTo(c);
		}

	}

	static class Resume extends AbstractLibFunction implements ProtectedResumable {

		@Override
		protected String name() {
			return "resume";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Coroutine coroutine = args.nextCoroutine();
			Object[] resumeArgs = args.copyRemaining();

			context.getReturnBuffer().setTo();

			try {
				context.resume(coroutine, resumeArgs);
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			ReturnBuffer rbuf = context.getReturnBuffer();
			ArrayList<Object> result = new ArrayList<>();
			result.add(Boolean.TRUE);
			result.addAll(Arrays.asList(rbuf.getAsArray()));
			rbuf.setToContentsOf(result);
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ResolvedControlThrowable {
			context.getReturnBuffer().setTo(Boolean.FALSE, error);
		}

	}

	static class Yield extends AbstractLibFunction {

		@Override
		protected String name() {
			return "yield";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			try {
				context.yield(args.copyAll());
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// no-op, the resume arguments are on the stack already
		}

	}

	static class IsYieldable extends AbstractLibFunction {

		@Override
		protected String name() {
			return "isyieldable";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			context.getReturnBuffer().setTo(context.isInMainCoroutine());
		}

	}

	static class Status extends AbstractLibFunction {

		public static final String STATUS_RUNNING = "running";
		public static final String STATUS_SUSPENDED = "suspended";
		public static final String STATUS_NORMAL = "normal";
		public static final String STATUS_DEAD = "dead";

		public static String status(ExecutionContext context, Coroutine coroutine) {
			switch (context.getCoroutineStatus(coroutine)) {
				case SUSPENDED:  return STATUS_SUSPENDED;
				case RUNNING:    return STATUS_RUNNING;
				case NORMAL:     return STATUS_NORMAL;
				case DEAD:       return STATUS_DEAD;
			}
			throw new AssertionError();  // should never happen
		}

		@Override
		protected String name() {
			return "status";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Coroutine coroutine = args.nextCoroutine();
			context.getReturnBuffer().setTo(status(context, coroutine));
		}

	}

	static class Running extends AbstractLibFunction {

		@Override
		protected String name() {
			return "running";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			Coroutine c = context.getCurrentCoroutine();
			context.getReturnBuffer().setTo(c, !context.isInMainCoroutine());
		}

	}

	static class Wrap extends AbstractLibFunction {

		@Override
		protected String name() {
			return "wrap";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			LuaFunction f = args.nextFunction();
			LuaFunction result = new WrappedCoroutine(f, context);
			context.getReturnBuffer().setTo(result);
		}

	}

	static class WrappedCoroutine extends AbstractFunctionAnyArg {

		private final Coroutine coroutine;

		public WrappedCoroutine(LuaFunction function, ExecutionContext context) {
			Objects.requireNonNull(function);
			Objects.requireNonNull(context);
			this.coroutine = context.newCoroutine(function);
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ResolvedControlThrowable {
			context.getReturnBuffer().setTo();
			try {
				context.resume(coroutine, args);
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			// no-op
		}

	}

}
