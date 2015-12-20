package net.sandius.rembulan;

import net.sandius.rembulan.util.ObjectSink;

public abstract class AbstractFuncVararg implements Func {

	@Override
	public void invoke(ObjectSink result) {
		invoke(result, new Object[] { });
	}

	@Override
	public void invoke(ObjectSink result, Object arg1) {
		invoke(result, new Object[] { arg1 });
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2) {
		invoke(result, new Object[] { arg1, arg2 });
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3) {
		invoke(result, new Object[] { arg1, arg2, arg3 });
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4) {
		invoke(result, new Object[] { arg1, arg2, arg3, arg4 });
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		invoke(result, new Object[] { arg1, arg2, arg3, arg4, arg5 });
	}

}
