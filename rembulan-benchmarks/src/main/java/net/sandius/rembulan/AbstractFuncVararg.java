package net.sandius.rembulan;

import net.sandius.rembulan.util.ObjectSink;

public abstract class AbstractFuncVararg implements Func {

	@Override
	public void invoke(ObjectSink result) {
		invoke(result, new Object[] { });
	}

	@Override
	public void invoke(ObjectSink result, Object a) {
		invoke(result, new Object[] { a });
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b) {
		invoke(result, new Object[] { a, b });
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b, Object c) {
		invoke(result, new Object[] { a, b, c });
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b, Object c, Object d) {
		invoke(result, new Object[] { a, b, c, d });
	}

	@Override
	public void invoke(ObjectSink result, Object a, Object b, Object c, Object d, Object e) {
		invoke(result, new Object[] { a, b, c, d, e });
	}

}
