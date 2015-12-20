package net.sandius.rembulan;

import net.sandius.rembulan.util.ObjectSink;

public abstract class AbstractFunc0 implements Func {

	@Override
	public void invoke(ObjectSink result, Object a) {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b) {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b, Object c) {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b, Object c, Object d) {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b, Object c, Object d, Object e) {
		invoke(result);
	}

	@Override
	public void invoke(ObjectSink result, Object[] args) {
		invoke(result);
	}

}
