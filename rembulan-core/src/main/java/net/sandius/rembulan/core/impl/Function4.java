package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;

public abstract class Function4 extends Function {

	@Override
	public void invoke(ExecutionContext context) throws ControlThrowable {
		invoke(context, null, null, null, null);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1) throws ControlThrowable {
		invoke(context, arg1, null, null, null);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2) throws ControlThrowable {
		invoke(context, arg1, arg2, null, null);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		invoke(context, arg1, arg2, arg3, null);
	}

	@Override
	public void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		invoke(context, arg1, arg2, arg3, arg4);
	}

	@Override
	public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
		Object a = Varargs.getElement(args, 0);
		Object b = Varargs.getElement(args, 1);;
		Object c = Varargs.getElement(args, 2);;
		Object d = Varargs.getElement(args, 3);;
		invoke(context, a, b, c, d);
	}

}
