package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Preemption;

public abstract class Function5 extends Function {

	@Override
	public Preemption invoke(ExecutionContext context) {
		return invoke(context, null, null, null, null, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1) {
		return invoke(context, arg1, null, null, null, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2) {
		return invoke(context, arg1, arg2, null, null, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3) {
		return invoke(context, arg1, arg2, arg3, null, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4) {
		return invoke(context, arg1, arg2, arg3, arg4, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object[] args) {
		Object a = Varargs.getElement(args, 0);
		Object b = Varargs.getElement(args, 1);;
		Object c = Varargs.getElement(args, 2);;
		Object d = Varargs.getElement(args, 3);;
		Object e = Varargs.getElement(args, 4);;
		return invoke(context, a, b, c, d, e);
	}

}
