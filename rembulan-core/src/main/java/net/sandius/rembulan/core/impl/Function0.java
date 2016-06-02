package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Preemption;

public abstract class Function0 extends Function {

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1) {
		return invoke(context);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2) {
		return invoke(context);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3) {
		return invoke(context);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4) {
		return invoke(context);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		return invoke(context);
	}

	@Override
	public Preemption invoke(ExecutionContext context, Object[] args) {
		return invoke(context);
	}

}
