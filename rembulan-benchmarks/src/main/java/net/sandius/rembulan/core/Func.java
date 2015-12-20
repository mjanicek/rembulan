package net.sandius.rembulan.core;

import net.sandius.rembulan.util.ObjectSink;

public interface Func {

	void invoke(LuaState state, ObjectSink result) throws ControlThrowable;

	void invoke(LuaState state, ObjectSink result, Object arg1) throws ControlThrowable;

	void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2) throws ControlThrowable;

	void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3) throws ControlThrowable;

	void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable;

	void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable;

	void invoke(LuaState state, ObjectSink result, Object[] args) throws ControlThrowable;

	void resume(LuaState state, ObjectSink result, Object suspendedState) throws ControlThrowable;

}
