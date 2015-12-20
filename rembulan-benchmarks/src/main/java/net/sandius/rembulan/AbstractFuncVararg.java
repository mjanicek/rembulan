package net.sandius.rembulan;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.util.ObjectSink;

public abstract class AbstractFuncVararg implements Func {

	@Override
	public void invoke(LuaState state, ObjectSink result) throws ControlThrowable {
		invoke(state, result, new Object[] { });
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1) throws ControlThrowable {
		invoke(state, result, new Object[] { arg1 });
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2) throws ControlThrowable {
		invoke(state, result, new Object[] { arg1, arg2 });
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		invoke(state, result, new Object[] { arg1, arg2, arg3 });
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		invoke(state, result, new Object[] { arg1, arg2, arg3, arg4 });
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		invoke(state, result, new Object[] { arg1, arg2, arg3, arg4, arg5 });
	}

}
