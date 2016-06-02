package net.sandius.rembulan.core;

public interface Invokable {

	Preemption invoke(ExecutionContext context);

	Preemption invoke(ExecutionContext context, Object arg1);

	Preemption invoke(ExecutionContext context, Object arg1, Object arg2);

	Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3);

	Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4);

	Preemption invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5);

	Preemption invoke(ExecutionContext context, Object[] args);

}
