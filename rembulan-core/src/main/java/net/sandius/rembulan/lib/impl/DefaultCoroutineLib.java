package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Preemption;
import net.sandius.rembulan.core.ProtectedResumable;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
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

	public static class Create extends LibFunction {

		public static final Create INSTANCE = new Create();

		@Override
		protected String name() {
			return "create";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Function func = args.nextFunction();
			Coroutine c = context.newCoroutine(func);
			context.getObjectSink().setTo(c);
			return null;
		}

	}

	public static class Resume extends LibFunction implements ProtectedResumable {

		public static final Resume INSTANCE = new Resume();

		@Override
		protected String name() {
			return "resume";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Coroutine coroutine = args.nextCoroutine();
			Object[] resumeArgs = args.getTail();

			context.getObjectSink().reset();

			Preemption.CoroutineSwitch.Resume p = new Preemption.CoroutineSwitch.Resume(coroutine, resumeArgs);
			return p.push(this, null);
		}

		@Override
		protected Preemption _resume(ExecutionContext context, Object suspendedState) {
			context.getObjectSink().prepend(Boolean.TRUE);
			return null;
		}

		@Override
		public Preemption resumeError(ExecutionContext context, Object suspendedState, Object error) {
			context.getObjectSink().setTo(Boolean.FALSE, error);
			return null;
		}

	}

	public static class Yield extends LibFunction {

		public static final Yield INSTANCE = new Yield();

		@Override
		protected String name() {
			return "yield";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Preemption.CoroutineSwitch.Yield p = new Preemption.CoroutineSwitch.Yield(args.getAll());
			return p.push(this, null);
		}

		@Override
		protected Preemption _resume(ExecutionContext context, Object suspendedState) {
			// no-op, the resume arguments are on the stack already
			return null;
		}

	}

	public static class IsYieldable extends LibFunction {

		public static final IsYieldable INSTANCE = new IsYieldable();

		@Override
		protected String name() {
			return "isyieldable";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			context.getObjectSink().setTo(context.canYield());
			return null;
		}

	}

	public static class Status extends LibFunction {

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
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Coroutine coroutine = args.nextCoroutine();
			context.getObjectSink().setTo(status(context, coroutine));
			return null;
		}

	}

	public static class Running extends LibFunction {

		public static final Running INSTANCE = new Running();

		@Override
		protected String name() {
			return "running";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Coroutine c = context.getCurrentCoroutine();
			context.getObjectSink().setTo(c, !c.canYield());
			return null;
		}

	}

	public static class Wrap extends LibFunction {

		public static final Wrap INSTANCE = new Wrap();

		static class WrappedCoroutine extends FunctionAnyarg {

			private final Coroutine coroutine;

			public WrappedCoroutine(Function function, ExecutionContext context) {
				Check.notNull(function);
				Check.notNull(context);
				this.coroutine = context.newCoroutine(function);
			}

			@Override
			public Preemption invoke(ExecutionContext context, Object[] args) {
				context.getObjectSink().reset();
				Preemption.CoroutineSwitch.Resume p = new Preemption.CoroutineSwitch.Resume(coroutine, args);
				return p.push(this, null);
			}

			@Override
			public Preemption resume(ExecutionContext context, Object suspendedState) {
				// no-op
				return null;
			}

		}

		@Override
		protected String name() {
			return "wrap";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Function f = args.nextFunction();
			Function result = new WrappedCoroutine(f, context);
			context.getObjectSink().setTo(result);
			return null;
		}

	}

}
