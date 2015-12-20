package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Func;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;

public abstract class AbstractFunc3 implements Func {

	@Override
	public void invoke(LuaState state, ObjectSink result) throws ControlThrowable {
		invoke(state, result, null, null, null);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1) throws ControlThrowable {
		invoke(state, result, arg1, null, null);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2) throws ControlThrowable {
		invoke(state, result, arg1, arg2, null);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		invoke(state, result, arg1, arg2, arg3);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		invoke(state, result, arg1, arg2, arg3);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object[] args) throws ControlThrowable {
		Object a = args.length >= 1 ? args[0] : null;
		Object b = args.length >= 2 ? args[1] : null;
		Object c = args.length >= 3 ? args[2] : null;
		invoke(state, result, a, b, c);
	}

}
