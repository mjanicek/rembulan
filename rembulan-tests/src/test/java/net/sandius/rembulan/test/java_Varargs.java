package net.sandius.rembulan.test;

import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Preemption;
import net.sandius.rembulan.core.impl.DefaultSavedState;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
import net.sandius.rembulan.core.impl.Varargs;

public class java_Varargs extends FunctionAnyarg {

	@Override
	public Preemption invoke(ExecutionContext context, Object[] args) {
		return run(context, Varargs.from(args, 0), null, null);
	}

	@Override
	public Preemption resume(ExecutionContext context, Object suspendedState) {
		DefaultSavedState ss = (DefaultSavedState) suspendedState;
		Object[] regs = ss.registers();
		return run(context, ss.varargs(), regs[0], regs[1]);
	}

	private Preemption run(ExecutionContext context, Object[] varargs, Object r_0, Object r_1) {
		throw new UnsupportedOperationException();
	}

}
