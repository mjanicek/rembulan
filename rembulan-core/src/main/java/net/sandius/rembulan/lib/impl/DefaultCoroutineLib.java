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

		public static String statusToString(Coroutine.Status status) {
			switch (status) {
				case Running:   return "running";
				case Suspended: return "suspended";
				case Normal:    return "normal";
				case Dead:      return "dead";
				default:  throw new IllegalArgumentException("Illegal status: " + status);
			}
		}

		@Override
		public void invoke(ExecutionContext context, Object arg1) throws ControlThrowable {
			Coroutine coroutine = LibUtils.checkArgument(arg1, 0, Coroutine.class);
			context.getObjectSink().setTo(statusToString(coroutine.getStatus()));
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
			context.getObjectSink().setTo(context.getCurrentCoroutine());
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Wrap extends Function1 {

		public static final Wrap INSTANCE = new Wrap();
		
		@Override
		public void invoke(ExecutionContext context, Object arg1) throws ControlThrowable {
			// TODO
			context.getObjectSink().setTo(arg1);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

}
