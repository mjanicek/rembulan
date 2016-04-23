package net.sandius.rembulan.core;

public interface Invokable {

	void invoke(ExecutionContext context) throws ControlThrowable;

	void invoke(ExecutionContext context, Object arg1) throws ControlThrowable;

	void invoke(ExecutionContext context, Object arg1, Object arg2) throws ControlThrowable;

	void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3) throws ControlThrowable;

	void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable;

	void invoke(ExecutionContext context, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable;

	void invoke(ExecutionContext context, Object[] args) throws ControlThrowable;

}
