package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Preemption;

public abstract class Function4 extends Function {

	@Override
	public Preemption invoke(ExecutionContext context) {
		return invoke(context, null, null, null, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1) {
		return invoke(context, arg1, null, null, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2) {
		return invoke(context, arg1, arg2, null, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3) {
		return invoke(context, arg1, arg2, arg3, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		return invoke(context, arg1, arg2, arg3, arg4);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object[] args) {
		Object a = Varargs.getElement(args, 0);
		Object b = Varargs.getElement(args, 1);;
		Object c = Varargs.getElement(args, 2);;
		Object d = Varargs.getElement(args, 3);;
		return invoke(context, a, b, c, d);
	}

}
