package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;

public abstract class AbstractFunc0 extends AbstractFunc {

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1) throws ControlThrowable {
		invoke(state, result);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2) throws ControlThrowable {
		invoke(state, result);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		invoke(state, result);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		invoke(state, result);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		invoke(state, result);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object[] args) throws ControlThrowable {
		invoke(state, result);
	}

}
