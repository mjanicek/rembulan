package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Preemption;

public abstract class Function3 extends Function {

	@Override
	public Preemption invoke(ExecutionContext context) {
		return invoke(context, null, null, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1) {
		return invoke(context, arg1, null, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2) {
		return invoke(context, arg1, arg2, null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4) {
		return invoke(context, arg1, arg2, arg3);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		return invoke(context, arg1, arg2, arg3);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object[] args) {
		Object a = args.length >= 1 ? args[0] : null;
		Object b = args.length >= 2 ? args[1] : null;
		Object c = args.length >= 3 ? args[2] : null;
		return invoke(context, a, b, c);
	}

}
