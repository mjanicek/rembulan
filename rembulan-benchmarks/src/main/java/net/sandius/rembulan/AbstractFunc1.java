package net.sandius.rembulan;

import net.sandius.rembulan.util.ObjectSink;

public abstract class AbstractFunc1 implements Func {

	@Override
	public void invoke(ObjectSink result) {
		invoke(result, null);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2) {
		invoke(result, arg1);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3) {
		invoke(result, arg1);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4) {
		invoke(result, arg1);
	}

	@Override
	public void invoke(ObjectSink result, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		invoke(result, arg1);
	}

	@Override
	public void invoke(ObjectSink result, Object[] args) {
		Object a = args.length >= 1 ? args[0] : null;
		invoke(result, a);
	}

}
