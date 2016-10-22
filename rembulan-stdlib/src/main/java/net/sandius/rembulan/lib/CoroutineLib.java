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
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This library comprises the operations to manipulate coroutines, which come inside the table
 * {@code coroutine}. See §2.6 of the Lua Reference Manual for a general description of coroutines.
 */
public final class CoroutineLib {

	/**
	 * {@code coroutine.create (f)}
	 *
	 * <p>Creates a new coroutine, with body {@code f}. {@code f} must be a function.
	 * Returns this new coroutine, an object with type {@code "thread"}.</p>
	 */
	public static final LuaFunction CREATE = new Create();

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
	 */
	public static final LuaFunction RESUME = new Resume();

	/**
	 * {@code coroutine.yield (···)}
	 *
	 * <p>Suspends the execution of the calling coroutine. Any arguments to {@code yield}
	 * are passed as extra results to {@code resume}.</p>
	 */
	public static final LuaFunction YIELD = new Yield();

	/**
	 * {@code coroutine.isyieldable ()}
	 *
	 * <p>Returns <b>true</b> when the running coroutine can yield.</p>
	 *
	 * <p>A running coroutine is yieldable if it is not the main thread and it is not inside
	 * a non-yieldable C function.</p>
	 */
	public static final LuaFunction ISYIELDABLE = new IsYieldable();

	/**
	 * {@code coroutine.status (co)}
	 *
	 * <p>Returns the status of coroutine {@code co}, as a string: {@code "running"},
	 * if the coroutine is running (that is, it called {@code status}); {@code "suspended"},
	 * if the coroutine is suspended in a call to {@code yield}, or if it has not started
	 * running yet; {@code "normal"} if the coroutine is active but not running (that is,
	 * it has resumed another coroutine); and {@code "dead"} if the coroutine has finished
	 * its body function, or if it has stopped with an error.</p>
	 */
	public static final LuaFunction STATUS = new Status();

	/**
	 * {@code coroutine.running ()}
	 *
	 * <p>Returns the running coroutine plus a boolean, <b>true</b> when the running coroutine
	 * is the main one.</p>
	 */
	public static final LuaFunction RUNNING = new Running();

	/**
	 * {@code coroutine.wrap (f)}
	 *
	 * <p>Creates a new coroutine, with body {@code f}. {@code f} must be a function.
	 * Returns a function that resumes the coroutine each time it is called. Any arguments
	 * passed to the function behave as the extra arguments to {@code resume}.
	 * Returns the same values returned by {@code resume}, except the first boolean. In case
	 * of error, propagates the error.</p>
	 */
	public static final LuaFunction WRAP = new Wrap();

	private CoroutineLib() {
		// not to be instantiated
	}

	public static void installInto(StateContext context, Table env) {
		Table t = context.newTable();

		t.rawset("create", CREATE);
		t.rawset("resume", RESUME);
		t.rawset("yield", YIELD);
		t.rawset("isyieldable", ISYIELDABLE);
		t.rawset("status", STATUS);
		t.rawset("running", RUNNING);
		t.rawset("wrap", WRAP);

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
			Object[] resumeArgs = args.getTail();

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
				context.yield(args.getAll());
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
			Check.notNull(function);
			Check.notNull(context);
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
