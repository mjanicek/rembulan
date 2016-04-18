package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;

public abstract class Function5 extends Function {

	@Override
	public void invoke(LuaState state, ObjectSink result) throws ControlThrowable {
		invoke(state, result, null, null, null, null, null);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1) throws ControlThrowable {
		invoke(state, result, arg1, null, null, null, null);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2) throws ControlThrowable {
		invoke(state, result, arg1, arg2, null, null, null);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		invoke(state, result, arg1, arg2, arg3, null, null);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		invoke(state, result, arg1, arg2, arg3, arg4, null);
	}

	@Override
	public void invoke(LuaState state, ObjectSink result, Object[] args) throws ControlThrowable {
		Object a = Varargs.getElement(args, 0);
		Object b = Varargs.getElement(args, 1);;
		Object c = Varargs.getElement(args, 2);;
		Object d = Varargs.getElement(args, 3);;
		Object e = Varargs.getElement(args, 4);;
		invoke(state, result, a, b, c, d, e);
	}

}
