package net.sandius.rembulan;

import net.sandius.rembulan.util.ObjectSink;

public abstract class AbstractFunc1 implements Func {

	@Override
	public void invoke(ObjectSink result) {
		invoke(result, null);
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b) {
		invoke(result, a);
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b, Object c) {
		invoke(result, a);
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b, Object c, Object d) {
		invoke(result, a);
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b, Object c, Object d, Object e) {
		invoke(result, a);
	}

	@Override
	public void invoke(ObjectSink result, Object[] args) {
		Object a = args.length >= 1 ? args[0] : null;
		invoke(result, a);
	}

}
