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

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.ProtectedResumable;
import net.sandius.rembulan.core.impl.AbstractFunctionAnyArg;
import net.sandius.rembulan.lib.CoroutineLib;
import net.sandius.rembulan.util.Check;

public class DefaultCoroutineLib extends CoroutineLib {

	@Override
	protected Function _create() {
		return Create.INSTANCE;
	}

	@Override
	protected Function _resume() {
		return Resume.INSTANCE;
	}

	@Override
	protected Function _yield() {
		return Yield.INSTANCE;
	}

	@Override
	protected Function _isyieldable() {
		return IsYieldable.INSTANCE;
	}

	@Override
	protected Function _status() {
		return Status.INSTANCE;
	}

	@Override
	protected Function _running() {
		return Running.INSTANCE;
	}

	@Override
	protected Function _wrap() {
		return Wrap.INSTANCE;
	}

	public static class Create extends AbstractLibFunction {

		public static final Create INSTANCE = new Create();

		@Override
		protected String name() {
			return "create";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Function func = args.nextFunction();
			Coroutine c = context.newCoroutine(func);
			context.getObjectSink().setTo(c);
		}

	}

	public static class Resume extends AbstractLibFunction implements ProtectedResumable {

		public static final Resume INSTANCE = new Resume();

		@Override
		protected String name() {
			return "resume";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Coroutine coroutine = args.nextCoroutine();
			Object[] resumeArgs = args.getTail();

			context.getObjectSink().reset();

			try {
				context.resume(coroutine, resumeArgs);
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			context.getObjectSink().prepend(Boolean.TRUE);
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ControlThrowable {
			context.getObjectSink().setTo(Boolean.FALSE, error);
		}

	}

	public static class Yield extends AbstractLibFunction {

		public static final Yield INSTANCE = new Yield();

		@Override
		protected String name() {
			return "yield";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			try {
				context.yield(args.getAll());
			}
			catch (ControlThrowable ct) {
				throw ct.push(this, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			// no-op, the resume arguments are on the stack already
		}

	}

	public static class IsYieldable extends AbstractLibFunction {

		public static final IsYieldable INSTANCE = new IsYieldable();

		@Override
		protected String name() {
			return "isyieldable";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			context.getObjectSink().setTo(context.canYield());
		}

	}

	public static class Status extends AbstractLibFunction {

		public static final Status INSTANCE = new Status();

		public static final String STATUS_RUNNING = "running";
		public static final String STATUS_SUSPENDED = "suspended";
		public static final String STATUS_NORMAL = "normal";
		public static final String STATUS_DEAD = "dead";

		public static String status(ExecutionContext context, Coroutine coroutine) {
			Check.notNull(context);
			Check.notNull(coroutine);

			Coroutine currentCoroutine = context.getCurrentCoroutine();

			if (coroutine == currentCoroutine) return STATUS_RUNNING;
			else if (coroutine.isDead()) return STATUS_DEAD;
			else if (coroutine.isResuming()) return STATUS_NORMAL;
			else return STATUS_SUSPENDED;
		}

		@Override
		protected String name() {
			return "status";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Coroutine coroutine = args.nextCoroutine();
			context.getObjectSink().setTo(status(context, coroutine));
		}

	}

	public static class Running extends AbstractLibFunction {

		public static final Running INSTANCE = new Running();

		@Override
		protected String name() {
			return "running";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Coroutine c = context.getCurrentCoroutine();
			context.getObjectSink().setTo(c, !c.canYield());
		}

	}

	public static class Wrap extends AbstractLibFunction {

		public static final Wrap INSTANCE = new Wrap();

		static class WrappedCoroutine extends AbstractFunctionAnyArg {

			private final Coroutine coroutine;

			public WrappedCoroutine(Function function, ExecutionContext context) {
				Check.notNull(function);
				Check.notNull(context);
				this.coroutine = context.newCoroutine(function);
			}

			@Override
			public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
				context.getObjectSink().reset();
				try {
					context.resume(coroutine, args);
				}
				catch (ControlThrowable ct) {
					throw ct.push(this, null);
				}
			}

			@Override
			public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
				// no-op
			}

		}

		@Override
		protected String name() {
			return "wrap";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Function f = args.nextFunction();
			Function result = new WrappedCoroutine(f, context);
			context.getObjectSink().setTo(result);
		}

	}

}
