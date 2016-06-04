package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
import net.sandius.rembulan.lib.LibUtils;

public abstract class LibFunction extends FunctionAnyarg {

	protected abstract String name();

	@Override
	public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
		CallArguments callArgs = new CallArguments(new LibUtils.NameMetamethodValueTypeNamer(context.getState()), name(), args);
		invoke(context, callArgs);
	}

	protected abstract void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable;

	@Override
	public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
		throw new NonsuspendableFunctionException(this.getClass());
	}

}
