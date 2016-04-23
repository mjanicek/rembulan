package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;

public abstract class Function0 extends Function {

	@Override
	public void invoke(ExecutionContext context, Object arg1) throws ControlThrowable {
		invoke(context);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2) throws ControlThrowable {
		invoke(context);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		invoke(context);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		invoke(context);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		invoke(context);
	}

	@Override
	public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
		invoke(context);
	}

}
