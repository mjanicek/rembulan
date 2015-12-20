package net.sandius.rembulan;

import net.sandius.rembulan.util.ObjectSink;

public abstract class AbstractFunc3 implements Func {

	@Override
	public void invoke(ObjectSink result) {
		invoke(result, null, null, null);
	}

	@Override
	public void invoke(ObjectSink result, Object a) {
		invoke(result, a, null, null);
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b) {
		invoke(result, a, b, null);
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b, Object c, Object d) {
		invoke(result, a, b, c);
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b, Object c, Object d, Object e) {
		invoke(result, a, b, c);
	}

	@Override
	public void invoke(ObjectSink result, Object[] args) {
		Object a = args.length >= 1 ? args[0] : null;
		Object b = args.length >= 2 ? args[1] : null;
		Object c = args.length >= 3 ? args[2] : null;
		invoke(result, a, b, c);
	}

}
