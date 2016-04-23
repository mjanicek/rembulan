package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;

public abstract class Function3 extends Function {

	@Override
	public void invoke(ExecutionContext context) throws ControlThrowable {
		invoke(context, null, null, null);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1) throws ControlThrowable {
		invoke(context, arg1, null, null);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2) throws ControlThrowable {
		invoke(context, arg1, arg2, null);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		invoke(context, arg1, arg2, arg3);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		invoke(context, arg1, arg2, arg3);
	}

	@Override
	public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
		Object a = args.length >= 1 ? args[0] : null;
		Object b = args.length >= 2 ? args[1] : null;
		Object c = args.length >= 3 ? args[2] : null;
		invoke(context, a, b, c);
	}

}
