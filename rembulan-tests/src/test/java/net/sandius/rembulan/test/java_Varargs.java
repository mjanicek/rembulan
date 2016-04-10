package net.sandius.rembulan.test;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.impl.DefaultSavedState;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
import net.sandius.rembulan.core.impl.Varargs;

import java.io.Serializable;

public class java_Varargs extends FunctionAnyarg {

	@Override
	public void invoke(LuaState state, ObjectSink result, Object[] args) throws ControlThrowable {
		run(state, result, Varargs.from(args, 0), null, null);
	}

	@Override
	public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
		DefaultSavedState ss = (DefaultSavedState) suspendedState;
		Object[] regs = ss.registers();
		run(state, result, ss.varargs(), regs[0], regs[1]);
	}

	private void run(LuaState state, ObjectSink result, Object[] varargs, Object r_0, Object r_1) throws ControlThrowable {
		throw new UnsupportedOperationException();
	}

}
