package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Preemption;

public abstract class FunctionAnyarg extends Function {

	@Override
	public Preemption invoke(ExecutionContext context) {
		return invoke(context, new Object[] { });
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1) {
		return invoke(context, new Object[] { arg1 });
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2) {
		return invoke(context, new Object[] { arg1, arg2 });
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3) {
		return invoke(context, new Object[] { arg1, arg2, arg3 });
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4) {
		return invoke(context, new Object[] { arg1, arg2, arg3, arg4 });
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		return invoke(context, new Object[] { arg1, arg2, arg3, arg4, arg5 });
	}

}
