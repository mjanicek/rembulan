package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;

public abstract class Function2 extends Function {

	@Override
	public void invoke(ExecutionContext context) throws ControlThrowable {
		invoke(context, null, null);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1) throws ControlThrowable {
		invoke(context, arg1, null);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		invoke(context, arg1, arg2);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		invoke(context, arg1, arg2);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		invoke(context, arg1, arg2);
	}

	@Override
	public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
		Object a = args.length >= 1 ? args[0] : null;
		Object b = args.length >= 2 ? args[1] : null;
		invoke(context, a, b);
	}

}
