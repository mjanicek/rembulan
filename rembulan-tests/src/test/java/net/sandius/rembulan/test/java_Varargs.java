package net.sandius.rembulan.test;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.impl.DefaultSavedState;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
import net.sandius.rembulan.core.impl.Varargs;

public class java_Varargs extends FunctionAnyarg {

	@Override
	public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
		run(context, Varargs.from(args, 0), null, null);
	}

	@Override
	public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
		DefaultSavedState ss = (DefaultSavedState) suspendedState;
		Object[] regs = ss.registers();
		run(context, ss.varargs(), regs[0], regs[1]);
	}

	private void run(ExecutionContext context, Object[] varargs, Object r_0, Object r_1) throws ControlThrowable {
		throw new UnsupportedOperationException();
	}

}
