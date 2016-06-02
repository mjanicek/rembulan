package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Preemption;

public abstract class Function1 extends Function {

	@Override
	public Preemption invoke(ExecutionContext context) {
		return invoke(context, (Object) null);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2) {
		return invoke(context, arg1);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3) {
		return invoke(context, arg1);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4) {
		return invoke(context, arg1);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		return invoke(context, arg1);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object[] args) {
		Object a = args.length >= 1 ? args[0] : null;
		return invoke(context, a);
	}

}
