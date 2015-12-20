package net.sandius.rembulan;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.util.ObjectSink;

public abstract class AbstractFunc0 implements Func {

	@Override
	public void invoke(ObjectSink result, Object arg1) throws ControlThrowable {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2) throws ControlThrowable {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object[] args) throws ControlThrowable {
		invoke(result);
	}

}
