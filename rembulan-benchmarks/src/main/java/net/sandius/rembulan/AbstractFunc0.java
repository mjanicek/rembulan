package net.sandius.rembulan;

import net.sandius.rembulan.util.ObjectSink;

public abstract class AbstractFunc0 implements Func {

	@Override
	public void invoke(ObjectSink result, Object arg1) {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2) {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3) {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4) {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object[] args) {
		invoke(result);
	}

}
