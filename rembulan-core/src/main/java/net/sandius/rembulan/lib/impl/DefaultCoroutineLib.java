package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.CoroutineSwitch;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.ProtectedResumable;
import net.sandius.rembulan.core.impl.Function0;
import net.sandius.rembulan.core.impl.Function1;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
import net.sandius.rembulan.core.impl.Varargs;
import net.sandius.rembulan.lib.CoroutineLib;
import net.sandius.rembulan.lib.LibUtils;
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

	public static class Create extends FunctionAnyarg {

		public static final Create INSTANCE = new Create();
		
		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Function func = LibUtils.checkFunction("create", args, 0);
			Coroutine c = context.newCoroutine(func);
			context.getObjectSink().setTo(c);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Resume extends FunctionAnyarg implements ProtectedResumable {

		public static final Resume INSTANCE = new Resume();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Coroutine coroutine = LibUtils.checkCoroutine("resume", args, 0);
			Object[] resumeArgs = Varargs.from(args, 1);

			context.getObjectSink().reset();

			CoroutineSwitch.Resume ct = new CoroutineSwitch.Resume(coroutine, resumeArgs);
			ct.push(this, null);

			throw ct;
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

	public static class Yield extends FunctionAnyarg {

		public static final Yield INSTANCE = new Yield();
		
		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			CoroutineSwitch.Yield ct = new CoroutineSwitch.Yield(args);
			ct.push(this, null);
			throw ct;
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
		}

	}

	public static class IsYieldable extends Function0 {

		public static final IsYieldable INSTANCE = new IsYieldable();
		
		@Override
		public void invoke(ExecutionContext context) throws ControlThrowable {
			context.getObjectSink().setTo(context.canYield());
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Status extends Function1 {

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
		public void invoke(ExecutionContext context, Object arg1) throws ControlThrowable {
			Coroutine coroutine = LibUtils.checkArgument(arg1, 0, Coroutine.class);
			context.getObjectSink().setTo(status(context, coroutine));
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Running extends Function0 {

		public static final Running INSTANCE = new Running();
		
		@Override
		public void invoke(ExecutionContext context) throws ControlThrowable {
			Coroutine c = context.getCurrentCoroutine();
			context.getObjectSink().setTo(c, !c.canYield());
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Wrap extends Function1 {

		public static final Wrap INSTANCE = new Wrap();

		static class WrappedCoroutine extends FunctionAnyarg {

			private final Coroutine coroutine;

			public WrappedCoroutine(Function function, ExecutionContext context) {
				Check.notNull(function);
				Check.notNull(context);
				this.coroutine = context.newCoroutine(function);
			}

			@Override
			public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
				context.getObjectSink().reset();

				CoroutineSwitch.Resume ct = new CoroutineSwitch.Resume(coroutine, args);
				ct.push(this, null);
				throw ct;
			}

			@Override
			public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
				// no-op
			}

		}

		@Override
		public void invoke(ExecutionContext context, Object arg1) throws ControlThrowable {
			Function f = LibUtils.checkFunction("coroutine.wrap", new Object[] {arg1}, 0);
			Function result = new WrappedCoroutine(f, context);
			context.getObjectSink().setTo(result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

}
